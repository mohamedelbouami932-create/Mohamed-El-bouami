package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.example.data.local.CheatFavoriteDao
import com.example.data.local.CollectibleProgressDao
import com.example.data.local.MissionProgressDao
import com.example.data.local.UserNoteDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

class SanAndreasRepository(
    private val context: Context,
    private val cheatDao: CheatFavoriteDao,
    private val collectibleDao: CollectibleProgressDao,
    private val missionDao: MissionProgressDao,
    private val noteDao: UserNoteDao
) {
    // ---- DEVICE SPECS & COMPATIBILITY ----
    fun checkDeviceSpecs(): DeviceSpecs {
        val statFs = StatFs(Environment.getDataDirectory().path)
        val blockSize = statFs.blockSizeLong
        val totalBlocks = statFs.blockCountLong
        val availableBlocks = statFs.availableBlocksLong

        val totalStorageGb = (totalBlocks * blockSize) / (1024f * 1024f * 1024f)
        val availableStorageGb = (availableBlocks * blockSize) / (1024f * 1024f * 1024f)

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availableRamGb = memInfo.availMem / (1024f * 1024f * 1024f)

        val packageManager = context.packageManager
        val isNetflixInstalled = isAppInstalled(packageManager, "com.netflix.NGP.GTASanAndreasDefinitiveEdition")
        val isClassicInstalled = isAppInstalled(packageManager, "com.rockstargames.gtasa")

        val cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: Build.CPU_ABI

        return DeviceSpecs(
            totalStorageGb = (totalStorageGb * 10f).roundToInt() / 10f,
            availableStorageGb = (availableStorageGb * 10f).roundToInt() / 10f,
            totalRamGb = (totalRamGb * 10f).roundToInt() / 10f,
            availableRamGb = (availableRamGb * 10f).roundToInt() / 10f,
            androidVersion = "Android ${Build.VERSION.RELEASE}",
            apiLevel = Build.VERSION.SDK_INT,
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            cpuAbi = cpuAbi,
            isNetflixVersionInstalled = isNetflixInstalled,
            isClassicVersionInstalled = isClassicInstalled
        )
    }

    private fun isAppInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    // ---- CHEATS & FAVORITES ----
    val favoriteCheatIds: Flow<List<String>> = cheatDao.getAllFavoriteIds()

    suspend fun toggleCheatFavorite(cheatId: String) = withContext(Dispatchers.IO) {
        if (cheatDao.isFavorite(cheatId)) {
            cheatDao.removeFavorite(cheatId)
        } else {
            cheatDao.addFavorite(CheatFavoriteEntity(cheatId = cheatId))
        }
    }

    // ---- COLLECTIBLES ----
    val collectibleProgress: Flow<Map<String, Boolean>> = collectibleDao.getAllProgress().map { list ->
        list.associate { it.id to it.isCollected }
    }

    suspend fun toggleCollectible(id: String, currentState: Boolean) = withContext(Dispatchers.IO) {
        collectibleDao.setProgress(CollectibleProgressEntity(id = id, isCollected = !currentState))
    }

    suspend fun resetAllCollectibles() = withContext(Dispatchers.IO) {
        collectibleDao.resetAll()
    }

    // ---- MISSIONS ----
    val missionProgress: Flow<Map<String, Boolean>> = missionDao.getAllProgress().map { list ->
        list.associate { it.id to it.isCompleted }
    }

    suspend fun toggleMissionCompleted(id: String, currentState: Boolean) = withContext(Dispatchers.IO) {
        missionDao.setProgress(MissionProgressEntity(id = id, isCompleted = !currentState))
    }

    suspend fun resetAllMissions() = withContext(Dispatchers.IO) {
        missionDao.resetAll()
    }

    // ---- USER NOTES ----
    val userNotes: Flow<List<UserNoteEntity>> = noteDao.getAllNotes()

    suspend fun saveNote(title: String, content: String, category: String) = withContext(Dispatchers.IO) {
        noteDao.insertNote(UserNoteEntity(title = title, content = content, category = category))
    }

    suspend fun deleteNote(note: UserNoteEntity) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    // ---- STATIC COMPENDIUM & GAME DATA ----
    fun getCheats(): List<CheatCode> = CHEAT_DATABASE

    fun getCollectibles(): List<CollectibleItem> = COLLECTIBLE_DATABASE

    fun getMissions(): List<MissionItem> = MISSION_DATABASE

    fun getVehicles(): List<VehicleInfo> = VEHICLE_DATABASE

    fun getWeapons(): List<WeaponInfo> = WEAPON_DATABASE

    fun getRadioStations(): List<RadioStationInfo> = RADIO_DATABASE

    companion object {
        private val CHEAT_DATABASE = listOf(
            CheatCode(
                id = "health_money",
                title = "Health, Armor & $250,000 Cash",
                description = "Instantly restores CJ's health and body armor to 100%, awards $250,000 cash, and repairs current vehicle if inside one.",
                category = CheatCategory.WEAPONS_HEALTH,
                codeAndroid = "Tap Cheat Keypad: HESOYAM",
                codePC = "HESOYAM",
                codePlayStation = "R1, R2, L1, X, Left, Down, Right, Up, Left, Down, Right, Up",
                codeXbox = "RT, RB, LT, A, Left, Down, Right, Up, Left, Down, Right, Up"
            ),
            CheatCode(
                id = "infinite_health",
                title = "Semi-Invulnerability (Infinite Health)",
                description = "Protects CJ against bullets, fire, and melee attacks (explosions and falls can still cause damage).",
                category = CheatCategory.WEAPONS_HEALTH,
                codeAndroid = "Tap Cheat Keypad: BAGUVIX",
                codePC = "BAGUVIX",
                codePlayStation = "Down, X, Right, Left, Right, R1, Right, Down, Up, Triangle",
                codeXbox = "Down, A, Right, Left, Right, RT, Right, Down, Up, Y"
            ),
            CheatCode(
                id = "weapons_pack_1",
                title = "Weapons Tier 1 (Thug Tools)",
                description = "Brass Knuckles, Baseball Bat, 9mm Pistol, Shotgun, Micro SMG, AK-47, Country Rifle, Rocket Launcher, Molotov Cocktails, Spray Can.",
                category = CheatCategory.WEAPONS_HEALTH,
                codeAndroid = "Tap Cheat Keypad: LXGIWYL",
                codePC = "LXGIWYL",
                codePlayStation = "R1, R2, L1, R2, Left, Down, Right, Up, Left, Down, Right, Up",
                codeXbox = "RT, RB, LT, RB, Left, Down, Right, Up, Left, Down, Right, Up"
            ),
            CheatCode(
                id = "weapons_pack_2",
                title = "Weapons Tier 2 (Professional Tools)",
                description = "Knife, Desert Eagle, Sawn-off Shotgun, TEC-9, M4 Carbine, Sniper Rifle, Flamethrower, Grenades, Fire Extinguisher.",
                category = CheatCategory.WEAPONS_HEALTH,
                codeAndroid = "Tap Cheat Keypad: PROFESSIONALSKIT",
                codePC = "PROFESSIONALSKIT (KJKSZPJ)",
                codePlayStation = "R1, R2, L1, R2, Left, Down, Right, Up, Left, Down, Down, Left",
                codeXbox = "RT, RB, LT, RB, Left, Down, Right, Up, Left, Down, Down, Left"
            ),
            CheatCode(
                id = "weapons_pack_3",
                title = "Weapons Tier 3 (Nutter Tools)",
                description = "Chainsaw, Silenced 9mm, Combat Shotgun, MP5, M4 Carbine, Heat-Seeking Rocket Launcher, Remote Explosives (Satchel Charges).",
                category = CheatCategory.WEAPONS_HEALTH,
                codeAndroid = "Tap Cheat Keypad: UZUMYMW",
                codePC = "UZUMYMW",
                codePlayStation = "R1, R2, L1, R2, Left, Down, Right, Up, Left, Down, Down, Down",
                codeXbox = "RT, RB, LT, RB, Left, Down, Right, Up, Left, Down, Down, Down"
            ),
            CheatCode(
                id = "infinite_ammo",
                title = "Infinite Ammo (No Reload Needed)",
                description = "Grants infinite ammunition for every weapon and removes the need to reload.",
                category = CheatCategory.WEAPONS_HEALTH,
                codeAndroid = "Tap Cheat Keypad: FULLCLIP",
                codePC = "FULLCLIP (WANRLTW)",
                codePlayStation = "L1, R1, Square, R1, Left, R2, R1, Left, Square, Down, L1, L1",
                codeXbox = "LT, RT, X, RT, Left, RB, RT, Left, X, Down, LT, LT"
            ),
            CheatCode(
                id = "jetpack",
                title = "Spawn Jetpack",
                description = "Spawns CJ's military experimental Jetpack right onto his back for unrestricted flight.",
                category = CheatCategory.VEHICLES,
                codeAndroid = "Tap Cheat Keypad: ROCKETMAN",
                codePC = "ROCKETMAN (YECGAA)",
                codePlayStation = "Left, Right, L1, L2, R1, R2, Up, Down, Left, Right",
                codeXbox = "Left, Right, LT, LB, RT, RB, Up, Down, Left, Right"
            ),
            CheatCode(
                id = "never_wanted",
                title = "Never Wanted (Lock 0 Stars)",
                description = "Completely disables police wanted stars; police will ignore any crimes committed.",
                category = CheatCategory.WANTED,
                codeAndroid = "Tap Cheat Keypad: AEZAKMI",
                codePC = "AEZAKMI",
                codePlayStation = "Circle, Right, Circle, Right, Left, Square, Triangle, Up",
                codeXbox = "B, Right, B, Right, Left, X, Y, Up"
            ),
            CheatCode(
                id = "six_stars",
                title = "Instant 6-Star Wanted Level",
                description = "Maxes out wanted level instantly; summons military tanks, helicopters, and SWAT.",
                category = CheatCategory.WANTED,
                codeAndroid = "Tap Cheat Keypad: BRINGITON",
                codePC = "BRINGITON (LJSPQK)",
                codePlayStation = "Circle, Right, Circle, Right, Left, Square, X, Down",
                codeXbox = "B, Right, B, Right, Left, X, A, Down"
            ),
            CheatCode(
                id = "clear_wanted",
                title = "Clear Wanted Level",
                description = "Instantly clears all active wanted stars back to zero.",
                category = CheatCategory.WANTED,
                codeAndroid = "Tap Cheat Keypad: ASNAEB",
                codePC = "ASNAEB (TURNDOWNTHEHEAT)",
                codePlayStation = "R1, R1, Circle, R2, Up, Down, Up, Down, Up, Down",
                codeXbox = "RT, RT, B, RB, Up, Down, Up, Down, Up, Down"
            ),
            CheatCode(
                id = "spawn_hydra",
                title = "Spawn Hydra Fighter Jet",
                description = "Spawns a VTOL Harrier-style military jumpjet equipped with lock-on missiles and flares.",
                category = CheatCategory.VEHICLES,
                codeAndroid = "Tap Cheat Keypad: JUMPJET",
                codePC = "JUMPJET",
                codePlayStation = "Triangle, Triangle, Square, Circle, X, L1, L1, Down, Up",
                codeXbox = "Y, Y, X, B, A, LT, LT, Down, Up"
            ),
            CheatCode(
                id = "spawn_rhino",
                title = "Spawn Rhino Tank",
                description = "Spawns an armored military Rhino Tank capable of blowing up vehicles upon collision.",
                category = CheatCategory.VEHICLES,
                codeAndroid = "Tap Cheat Keypad: AIWPRTON",
                codePC = "AIWPRTON",
                codePlayStation = "Circle, Circle, L1, Circle, Circle, Circle, L1, L2, R1, Triangle, Circle, Triangle",
                codeXbox = "B, B, LT, B, B, B, LT, LB, RT, Y, B, Y"
            ),
            CheatCode(
                id = "spawn_hunter",
                title = "Spawn Hunter Attack Helicopter",
                description = "Spawns Apache-style attack chopper armed with minigun and rapid homing missiles.",
                category = CheatCategory.VEHICLES,
                codeAndroid = "Tap Cheat Keypad: OHDUDE",
                codePC = "OHDUDE",
                codePlayStation = "Circle, X, L1, Circle, Circle, L1, Circle, R1, R2, L2, L1, L1",
                codeXbox = "B, A, LT, B, B, LT, B, RT, RB, LB, LT, LT"
            ),
            CheatCode(
                id = "flying_cars",
                title = "Flying Cars",
                description = "Vehicles gain airplane aerodynamics and can take off into the sky when driving fast.",
                category = CheatCategory.VEHICLES,
                codeAndroid = "Tap Cheat Keypad: RIPAZHA",
                codePC = "RIPAZHA (CHITTYCHITTYBANGBANG)",
                codePlayStation = "Square, Down, L2, Up, L1, Circle, Up, X, Left",
                codeXbox = "X, Down, LB, Up, LT, B, Up, A, Left"
            ),
            CheatCode(
                id = "super_punch",
                title = "Super Mega Punch",
                description = "CJ's melee attacks send enemies, civilians, and vehicles flying across the street.",
                category = CheatCategory.CJ_STATS,
                codeAndroid = "Tap Cheat Keypad: IAVENJQ",
                codePC = "IAVENJQ (STINGLIKEABEE)",
                codePlayStation = "Up, Left, X, Triangle, R1, Circle, Circle, Circle, L2",
                codeXbox = "Up, Left, A, Y, RT, B, B, B, LB"
            ),
            CheatCode(
                id = "max_muscle",
                title = "Maximum Muscle Stat",
                description = "Instantly maxes out CJ's muscle meter for maximum physical strength and physique.",
                category = CheatCategory.CJ_STATS,
                codeAndroid = "Tap Cheat Keypad: BUFFMEUP",
                codePC = "BUFFMEUP (JYSDSOD)",
                codePlayStation = "Triangle, Up, Up, Left, Right, Square, Circle, Left",
                codeXbox = "Y, Up, Up, Left, Right, X, B, Left"
            ),
            CheatCode(
                id = "sunny_weather",
                title = "Sunny & Clear California Weather",
                description = "Clears any storm and sets the sky to bright sunny afternoon.",
                category = CheatCategory.WEATHER,
                codeAndroid = "Tap Cheat Keypad: PLEASANTLYWARM",
                codePC = "PLEASANTLYWARM (AFZLLQLL)",
                codePlayStation = "R2, X, L1, L1, L2, L2, L2, Down",
                codeXbox = "RB, A, LT, LT, LB, LB, LB, Down"
            ),
            CheatCode(
                id = "riot_mode",
                title = "Pedestrian Riot Mode (Chaos)",
                description = "Pedestrians riot in the streets with weapons and attack each other (like Act 6 LS riots).",
                category = CheatCategory.PEDS_WORLD,
                codeAndroid = "Tap Cheat Keypad: STATEOFEMERGENCY",
                codePC = "STATEOFEMERGENCY (IOJXZVS)",
                codePlayStation = "Down, Left, Up, Left, X, R2, R1, L2, L1",
                codeXbox = "Down, Left, Up, Left, A, RB, RT, LB, LT"
            )
        )

        private val COLLECTIBLE_DATABASE = listOf(
            CollectibleItem("tag_01", CollectibleType.TAGS, 1, "Ganton", "Behind Johnson House on the wall facing the train tracks.", "Check the rear brick fence near the canal."),
            CollectibleItem("tag_02", CollectibleType.TAGS, 2, "Ganton", "On the south side of the overpass highway pillar.", "Drive south from Grove Street towards Idlewood."),
            CollectibleItem("tag_03", CollectibleType.TAGS, 3, "Idlewood", "On the wall of the Barber Shop in Idlewood.", "Where CJ gets his first haircut with Ryder."),
            CollectibleItem("tag_04", CollectibleType.TAGS, 4, "Idlewood", "On the side of the 24-7 Supermarket wall.", "Near the gas station convenience store."),
            CollectibleItem("tag_05", CollectibleType.TAGS, 5, "East Los Santos", "On the wall behind the Cluckin' Bell alley.", "Behind the drive-thru lane."),
            CollectibleItem("tag_06", CollectibleType.TAGS, 6, "East Los Santos", "High wall next to the basketball court in East LS.", "Need to climb the dumpster or low wall to spray."),
            CollectibleItem("tag_07", CollectibleType.TAGS, 7, "Glen Park", "Under the bridge in the middle of Glen Park lake.", "Right next to the central park gazebo."),
            CollectibleItem("tag_08", CollectibleType.TAGS, 8, "Willowfield", "On the side of the Ammu-Nation store.", "Facing the parking lot near the shooting range."),
            CollectibleItem("tag_09", CollectibleType.TAGS, 9, "Corona", "Near Cesar's house on the southern driveway wall.", "In the heart of the Varrios Los Aztecas hood."),
            CollectibleItem("tag_10", CollectibleType.TAGS, 10, "Santa Maria Beach", "On the side of the beachside boardwalk motel.", "Near the lifeguard station and stairs down to the sand."),

            CollectibleItem("snap_01", CollectibleType.SNAPSHOTS, 1, "Doherty", "Floating directly above the Doherty garage crane structure.", "Use camera zoom from the street level."),
            CollectibleItem("snap_02", CollectibleType.SNAPSHOTS, 2, "Downtown SF", "Above the spiraling peak of the Transamerica Pyramid.", "Stand across the avenue looking up toward the spire."),
            CollectibleItem("snap_03", CollectibleType.SNAPSHOTS, 3, "Gant Bridge", "On top of the first south suspension cable tower.", "Zoom in with camera from the bridge deck walkway."),
            CollectibleItem("snap_04", CollectibleType.SNAPSHOTS, 4, "Chinatown", "Above the iconic Chinatown pagoda archway entrance.", "Directly facing the ornate red arch."),
            CollectibleItem("snap_05", CollectibleType.SNAPSHOTS, 5, "Pier 69", "Over the large Pier 69 wood sign in front of the bay.", "Where the mission Pier 69 takes place."),

            CollectibleItem("shoe_01", CollectibleType.HORSESHOES, 1, "The Strip", "On the rooftop sign of The Camel's Toe pyramid casino.", "Use a Jetpack or parachute from a plane."),
            CollectibleItem("shoe_02", CollectibleType.HORSESHOES, 2, "The Strip", "Inside the Four Dragons Casino main entrance fountain.", "Glowing in the middle of the dragon pond."),
            CollectibleItem("shoe_03", CollectibleType.HORSESHOES, 3, "Caligula's", "On the upper balcony terrace above Caligula's Palace.", "Accessible by stairwell or jetpack."),
            CollectibleItem("shoe_04", CollectibleType.HORSESHOES, 4, "Come-A-Lot", "On top of the medieval fortress castle tower.", "Fly up or climb the castle parapet."),
            CollectibleItem("shoe_05", CollectibleType.HORSESHOES, 5, "Pilson Way", "Underneath the highway flyover overpass intersection.", "Hidden behind concrete support columns."),

            CollectibleItem("oyst_01", CollectibleType.OYSTERS, 1, "Fisherman's Lagoon", "Submerged beneath the wooden dock pier.", "Dive deep near Palomino Creek."),
            CollectibleItem("oyst_02", CollectibleType.OYSTERS, 2, "Los Santos Marina", "Deep in the water by the Santa Maria lighthouse.", "Look for the glow underwater near the reef."),
            CollectibleItem("oyst_03", CollectibleType.OYSTERS, 3, "San Fierro Bay", "Under the Gant Bridge midpoint near the tanker ship.", "High lung capacity or scuba swim recommended."),
            CollectibleItem("oyst_04", CollectibleType.OYSTERS, 4, "Sherman Dam", "At the bottom of the intake reservoir turbine face.", "Beneath the massive concrete hydro dam."),
            CollectibleItem("oyst_05", CollectibleType.OYSTERS, 5, "Verdant Bluffs", "Underwater in the coastal channel between LS and Red County.", "Swim along the rocky shoreline.")
        )

        private val MISSION_DATABASE = listOf(
            MissionItem(
                id = "m_intro",
                act = MissionAct.ACT_1_LOS_SANTOS,
                title = "In the Beginning & Big Smoke",
                giver = "Carl Johnson & Sweet",
                reward = "+ Respect",
                difficulty = "Easy",
                briefing = "CJ returns to Grove Street for his mother's funeral and reunites with Sweet, Big Smoke, and Ryder.",
                walkthroughTip = "Follow Sweet on the BMX bicycle back to Grove Street. Pump the sprint button steadily to evade the Ballas car."
            ),
            MissionItem(
                id = "m_drive_thru",
                act = MissionAct.ACT_1_LOS_SANTOS,
                title = "Drive-Thru",
                giver = "Sweet",
                reward = "+$200, + Respect",
                difficulty = "Medium",
                briefing = "The boys visit Cluckin' Bell when a rival Ballas car ambushes them.",
                walkthroughTip = "Stay right behind the Ballas Glendale while Sweet and Ryder shoot. Don't let Smoke eat all the fries!"
            ),
            MissionItem(
                id = "m_wrong_side",
                act = MissionAct.ACT_1_LOS_SANTOS,
                title = "Wrong Side of the Tracks",
                giver = "Big Smoke",
                reward = "+ Respect",
                difficulty = "Infamous",
                briefing = "Chase down the Los Santos Vagos gang escaping on top of a moving train.",
                walkthroughTip = "Crucial tip: Do NOT ride right beside the train carriage! Ride far on the right track next to the train so Big Smoke has an open firing angle over the side."
            ),
            MissionItem(
                id = "m_green_sabre",
                act = MissionAct.ACT_1_LOS_SANTOS,
                title = "The Green Sabre",
                giver = "Sweet / Cesar",
                reward = "Act 1 Finale Climax",
                difficulty = "Hard",
                briefing = "Cesar reveals a devastating betrayal involving the Green Sabre. CJ rushes to save Sweet under the Mulholland Intersection.",
                walkthroughTip = "Bring full body armor and an AK-47. Take cover behind CJ's car and prioritize Ballas carrying automatic weapons."
            ),
            MissionItem(
                id = "m_badlands",
                act = MissionAct.ACT_2_BADLANDS,
                title = "Badlands",
                giver = "Officer Tenpenny (C.R.A.S.H.)",
                reward = "Countryside Unlocked",
                difficulty = "Medium",
                briefing = "Tenpenny forces CJ to eliminate a former police officer hiding in a remote cabin atop Mount Chiliad.",
                walkthroughTip = "Equip a Sniper Rifle from a distance or shoot the target's car tires before he flees down the winding mountain trail."
            ),
            MissionItem(
                id = "m_body_harvest",
                act = MissionAct.ACT_2_BADLANDS,
                title = "Body Harvest",
                giver = "The Truth",
                reward = "+ Respect",
                difficulty = "Medium",
                briefing = "Steal a massive Combine Harvester from an armed survivalist cult farm in Flint County.",
                walkthroughTip = "Snipe the workers with rifles from outside the gate, then drive the harvester right out without stopping."
            ),
            MissionItem(
                id = "m_zero_supply",
                act = MissionAct.ACT_3_SAN_FIERRO,
                title = "Supply Lines...",
                giver = "Zero",
                reward = "+$5,000 & Zero's Shop Asset",
                difficulty = "Infamous",
                briefing = "Pilot an RC Baron plane to take out Berkley's delivery couriers before fuel expires.",
                walkthroughTip = "Conserve fuel: Release the throttle and glide whenever flying level. Land behind couriers to shoot them from the street."
            ),
            MissionItem(
                id = "m_pier69",
                act = MissionAct.ACT_3_SAN_FIERRO,
                title = "Pier 69",
                giver = "Wu Zi Mu & Cesar",
                reward = "+$15,000 & + Respect",
                difficulty = "Hard",
                briefing = "Ambush the Loc Syndicate meeting on the Pier and settle scores with Ryder and T-Bone Mendez.",
                walkthroughTip = "Use the rooftop sniper rifle to take out guards. When Ryder dives into the water, shoot him before he gets into his boat or use an RPG."
            ),
            MissionItem(
                id = "m_flight_school",
                act = MissionAct.ACT_4_DESERT,
                title = "Learning to Fly (Pilot School)",
                giver = "Mike Toreno",
                reward = "Pilot License & Airstrip Asset",
                difficulty = "Hard",
                briefing = "Complete 10 flight tests at Verdant Meadows abandoned airfield.",
                walkthroughTip = "Use gentle rudder taps. For the helicopter destroy-targets test, use third-person view and level before firing."
            ),
            MissionItem(
                id = "m_freefall",
                act = MissionAct.ACT_5_LAS_VENTURAS,
                title = "Freefall",
                giver = "Salvatore Leone",
                reward = "+$15,000 & + Respect",
                difficulty = "Infamous",
                briefing = "Intercept the Forelli hit squad plane arriving at Las Venturas airport while flying a slow Dodo.",
                walkthroughTip = "Fly HIGH above the incoming jet before it appears on radar. When you spot it, dive downwards into the red corona halo to gain maximum speed!"
            ),
            MissionItem(
                id = "m_caligula_heist",
                act = MissionAct.ACT_5_LAS_VENTURAS,
                title = "Breaking the Bank at Caligula's",
                giver = "Wu Zi Mu",
                reward = "+$100,000 & Master Heist",
                difficulty = "Hard",
                briefing = "Execute the grand casino heist with Zero, Woozie, and the team. Infiltrate the vault and escape.",
                walkthroughTip = "Throw tear gas through the air ducts, protect the armored van, and remember to deploy your parachute when escaping off the casino roof."
            ),
            MissionItem(
                id = "m_end_of_line",
                act = MissionAct.ACT_6_RETURN_LS,
                title = "End of the Line (Grand Finale)",
                giver = "Sweet",
                reward = "100% Story Completion",
                difficulty = "Epic",
                briefing = "Storm Big Smoke's 4-story crack fortress, confront Smoke, escape the raging inferno, and chase Officer Tenpenny through the burning riots.",
                walkthroughTip = "Equip Night Vision goggles for the dark floor. Carry full M4 and combat shotgun ammo. During the fire escape, use the fire extinguisher to clear doorways."
            )
        )

        private val VEHICLE_DATABASE = listOf(
            VehicleInfo("Infernus", "Supercar", 10, 9, "The fastest exotic supercar in San Andreas. Mid-engine with scissor doors.", "Paradiso (San Fierro) & The Strip (Las Venturas)", null),
            VehicleInfo("Bullet", "Supercar", 9, 10, "American supercar modeled after the Ford GT. Exceptional acceleration and grip.", "Get all silver in Driving School, Burger Shot Doherty", null),
            VehicleInfo("Turismo", "Supercar", 10, 8, "Legendary Italian supercar with immense top speed and sleek low profile.", "The Camel's Toe casino garage, Las Venturas", null),
            VehicleInfo("Hydra", "Military Aircraft", 10, 9, "Vertical Take-Off and Landing (VTOL) jumpjet with guided heat-seeking missiles.", "Easter Basin Naval Base & Verdant Meadows Airstrip", "JUMPJET"),
            VehicleInfo("Hunter", "Attack Helicopter", 9, 8, "Apache combat gunship equipped with rapid-fire 30mm cannon and rockets.", "Gold medals in Flight School, K.A.C.C. Military Depot", "OHDUDE"),
            VehicleInfo("Rhino Tank", "Military Heavy", 4, 10, "Heavy armored tank. Fires high-explosive shells and crushes cars on impact.", "Area 69 Restricted Compound (or 6 Stars Wanted)", "AIWPRTON"),
            VehicleInfo("NRG-500", "Racing Motorcycle", 10, 9, "Premier 500cc racing superbike with screaming acceleration and wheelie capability.", "Dry dock in San Fierro & multi-story car park LS", null),
            VehicleInfo("Jetpack", "Experimental Equipment", 7, 10, "Secret military flight apparatus from Area 69. Free omni-directional flight.", "Verdant Meadows airstrip safehouse", "ROCKETMAN"),
            VehicleInfo("Monster Truck", "Off-Road", 6, 8, "Massive 66-inch wheels capable of rolling over standard passenger vehicles.", "Flint County trailer park & Michelle Cannes safehouse", "MONSTERMASH"),
            VehicleInfo("Vortex", "Hovercraft", 7, 7, "Amphibious craft that glides seamlessly across land, asphalt, sand, and water.", "Bayside Marina pier & Verdant Meadows hangar", "KGGGDKP")
        )

        private val WEAPON_DATABASE = listOf(
            WeaponInfo("Minigun (Vulcan)", "Heavy", 10, "500 rds", "The ultimate weapon in San Andreas. Shreds vehicles, helicopters, and crowds in seconds.", "Area 69 underground bunker & Kincaid Rail Bridge arch"),
            WeaponInfo("Desert Eagle", "Pistols", 9, "7 rds", "High-caliber magnum handgun. Kills standard enemies with a single upper-body hit.", "Corner of fence in Ocean Docks & Gang Tag reward"),
            WeaponInfo("Combat Shotgun (SPAS-12)", "Shotguns", 9, "7 rds", "Rapid-fire semi-automatic tactical shotgun with punishing close-range spread.", "Four Dragons casino roof & LV Police Station"),
            WeaponInfo("M4 Carbine", "Assault Rifles", 9, "50 rds", "Military assault rifle with long range, pin-point precision, and rapid fire.", "Los Santos International runway & Area 69 checkpoint"),
            WeaponInfo("Sniper Rifle", "Rifles", 10, "1 rd", "High-power bolt-action rifle with variable zoom telescopic scope.", "Vinewood sign roof & Doherty garage rooftop"),
            WeaponInfo("Satchel Charges", "Heavy / Thrown", 10, "Remote", "Plastique explosive charges detonated remotely via radio frequency trigger.", "Behind Four Dragons casino & Montgomery alley"),
            WeaponInfo("Katana", "Melee", 8, "N/A", "Traditional razor-sharp Japanese blade with decapitation finishing moves.", "Alleyway in Chinatown, San Fierro")
        )

        private val RADIO_DATABASE = listOf(
            RadioStationInfo(
                "Radio Los Santos",
                "106.1 FM",
                "90s West Coast Hip Hop",
                "Julio G",
                listOf("N.W.A - Express Yourself", "Dr. Dre & Snoop Dogg - Nuthin' but a 'G' Thang", "2Pac - I Don't Give a Fuck", "Ice Cube - Check Yo Self"),
                "The authentic soundtrack of Grove Street and 1992 Los Santos culture."
            ),
            RadioStationInfo(
                "K-DST (The Dust)",
                "91.5 FM",
                "Classic Rock & Road Anthems",
                "Tommy 'The Nightmare' Smith (Axl Rose)",
                listOf("Lynyrd Skynyrd - Free Bird", "Tom Petty - Runnin' Down a Dream", "America - A Horse with No Name", "The Who - Eminence Front"),
                "Dusty highway rock for cruising across the Bone County desert and Red County mountains."
            ),
            RadioStationInfo(
                "Radio X",
                "104.9 FM",
                "Modern Rock & Grunge",
                "Sage",
                listOf("Soundgarden - Rusty Cage", "Rage Against the Machine - Killing in the Name", "Alice in Chains - Them Bones", "Stone Temple Pilots - Plush"),
                "Hard-hitting 90s alternative rock, heavy metal, and grunge revolution."
            ),
            RadioStationInfo(
                "Bounce FM",
                "92.1 FM",
                "Funk & Soul Grooves",
                "The Funktipus (George Clinton)",
                listOf("Kool & the Gang - Hollywood Swinging", "The Gap Band - You Dropped a Bomb on Me", "Rick James - Cold Blooded", "Zapp - I Can Make You Dance"),
                "The mothership of funk, lowrider hydraulics, and pure groove."
            ),
            RadioStationInfo(
                "SF-UR",
                "98.9 FM",
                "Chicago House & Club Music",
                "Hans Oberlander",
                listOf("A Guy Called Gerald - Voodoo Ray", "808 State - Pacific 202", "Frankie Knuckles - Your Love", "Marshall Jefferson - Move Your Body"),
                "Underground acid house and European club beats booming from San Fierro docks."
            ),
            RadioStationInfo(
                "K-Rose",
                "106.9 FM",
                "Classic Country & Western",
                "Mary-Beth Maybell",
                listOf("Willie Nelson - Crazy", "Patsy Cline - Three Cigarettes in an Ashtray", "Conway Twitty - Louisiana Woman", "Juice Newton - Queen of Hearts"),
                "Country heartache and honky-tonk ballads broadcasting from Bone County."
            )
        )
    }
}
