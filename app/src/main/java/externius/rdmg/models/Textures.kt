package externius.rdmg.models

import kotlinx.serialization.Serializable

@Serializable
enum class Textures {
    EDGE, MARBLE, CORRIDOR, DOOR, ROOM, ENTRY, TRAP, ROOM_EDGE, NO_CORRIDOR_DOOR, DOOR_LOCKED, DOOR_TRAPPED, NO_CORRIDOR_DOOR_LOCKED, NO_CORRIDOR_DOOR_TRAPPED, ROAMING_MONSTER
}
