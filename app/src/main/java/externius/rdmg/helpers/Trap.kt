package externius.rdmg.helpers

internal object Trap {
    private val trapSeverity = arrayOf("Setback", "Dangerous", "Deadly")
    private val trapSave = intArrayOf(10, 12, 16, 21)
    private val trapAttackBonus = intArrayOf(3, 6, 9, 13)
    private val trapDmgSeverity = arrayOf(
        intArrayOf(1, 2, 4),
        intArrayOf(2, 4, 10),
        intArrayOf(4, 10, 18),
        intArrayOf(10, 18, 24)
    )

    private val trapKind = arrayOf(
        arrayOf("Collapsing Roof", "Dexterity", "10", "15", "Dexterity", "false", "bludgeoning", ""),
        arrayOf("Falling Net", "Strength", "10", "15", "Dexterity", "false", "", "restrained."),
        arrayOf("Fire-Breathing Statue", "Dexterity", "15", "13", "Dispel Magic", "false", "fire", ""),
        arrayOf("Spiked Pit", "Constitution", "15", "15", "Intelligence", "false", "piercing", ""),
        arrayOf("Locking Pit", "Strength", "10", "15", "Intelligence", "false", "", "locked."),
        arrayOf("Poison Darts", "Constitution", "15", "15", "Intelligence", "true", "poison", ""),
        arrayOf("Poison Needle", "Constitution", "15", "15", "Dexterity", "false", "poison", ""),
        arrayOf("Rolling Sphere", "Dexterity", "15", "15", "Intelligence", "false", "bludgeoning", "")
    )

    private val trapDoorKind = arrayOf(
        arrayOf("Fire trap", "Dexterity", "10", "15", "Intelligence", "false", "fire", ""),
        arrayOf("Lock Covered in Dragon Bile", "Constitution", "10", "15", "Intelligence", "false", "poison", ""),
        arrayOf("Hail of Needles", "Dexterity", "15", "13", "Dexterity", "false", "piercing", ""),
        arrayOf("Stone Blocks from Ceiling", "Dexterity", "15", "15", "Intelligence", "true", "bludgeoning", ""),
        arrayOf("Doorknob Smeared with Contact Poison", "Constitution", "15", "10", "Intelligence", "false", "poison", ""),
        arrayOf("Poison Darts", "Constitution", "15", "15", "Intelligence", "true", "poison", ""),
        arrayOf("Poison Needle", "Constitution", "15", "15", "Dexterity", "false", "poison", ""),
        arrayOf("Energy Drain", "Constitution", "15", "15", "Dispel Magic", "false", "necrotic", "")
    )

    private var currentTrap: Array<String>? = null

    private fun getTrapAttackBonus(trapDanger: Int): String {
        val trap = currentTrap ?: return "."
        return if (trap[5].toBoolean()) {
            val min = trapAttackBonus[trapDanger]
            val max = trapAttackBonus[trapDanger + 1]
            ", (attack bonus +" + Utils.getRandomInt(min, max) + ")."
        } else {
            "."
        }
    }

    private fun getTrapSaveDC(trapDanger: Int): Int {
        val min = trapSave[trapDanger]
        val max = trapSave[trapDanger + 1]
        return Utils.getRandomInt(min, max)
    }

    private fun getTrapDamage(trapDanger: Int): Int {
        return if (Utils.partyLevel < 5) {
            trapDmgSeverity[0][trapDanger]
        } else if (Utils.partyLevel < 11) {
            trapDmgSeverity[1][trapDanger]
        } else if (Utils.partyLevel < 17) {
            trapDmgSeverity[2][trapDanger]
        } else {
            trapDmgSeverity[3][trapDanger]
        }
    }

    @JvmStatic
    fun getTrapName(count: Int): String {
        return "#TRAP$count#"
    }

    private fun getTrapDanger(): Int {
        return when (Utils.dungeonDifficulty) {
            0 -> Utils.getRandomInt(0, 1)
            1, 2 -> Utils.getRandomInt(0, 2)
            3 -> Utils.getRandomInt(0, 3)
            else -> 0
        }
    }

    @JvmStatic
    fun getCurrentTrap(door: Boolean): String {
        val trapDanger = getTrapDanger()
        val trap = if (door) {
            trapDoorKind[Utils.getRandomInt(0, trapDoorKind.size)]
        } else {
            trapKind[Utils.getRandomInt(0, trapKind.size)]
        }
        currentTrap = trap
        return if (trap[6].isNotEmpty()) {
            "${trap[0]} [${trapSeverity[trapDanger]}]: DC ${trap[2]} to spot, DC ${trap[3]} to disable (${trap[4]}), DC ${getTrapSaveDC(trapDanger)} ${trap[1]} save or take ${getTrapDamage(trapDanger)}D10 (${trap[6]}) damage${getTrapAttackBonus(trapDanger)}"
        } else {
            "${trap[0]} [${trapSeverity[trapDanger]}]: DC ${trap[2]} to spot, DC ${trap[3]} to disable (${trap[4]}), DC ${getTrapSaveDC(trapDanger)} ${trap[1]} save or ${trap[7]}"
        }
    }
}
