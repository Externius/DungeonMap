package externius.rdmg.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
class DungeonTile {
    var texture: Textures = Textures.EDGE
    var i: Int = 0
    var j: Int = 0
    var x: Int = 0
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0
    var h: Int = 0
    var g: Int = 0
    var f: Int = 0

    @Transient
    var parent: DungeonTile? = null
    var description: String? = null
    var index: Int = 0

    constructor()

    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
        this.texture = Textures.EDGE
        h = 0
    }

    constructor(i: Int, j: Int, x: Int, y: Int, width: Int, height: Int) {
        this.i = i
        this.j = j
        this.x = x
        this.y = y
        this.width = width
        this.height = height
        this.texture = Textures.MARBLE
        h = 0
    }
}
