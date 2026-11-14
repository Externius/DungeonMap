package externius.rdmg.models

sealed class DescriptionItem {
    data class Room(val room: RoomDescription) : DescriptionItem()
    data class Trap(val trap: TrapDescription) : DescriptionItem()
    data class Monster(val monster: RoamingMonsterDescription) : DescriptionItem()
}
