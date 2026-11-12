package externius.rdmg.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EditPopupState(
    val index: Int,
    val texture: Textures,
    val monsterText: String? = null,
    val treasureText: String? = null,
    val descriptionText: String? = null,
) : Parcelable
