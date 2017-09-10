package io.devholic.epilogue.entity


data class Recipient(
    val id: String,
    val birthday: String,
    val enterDate: String,
    val name: String,
    val regiment: String,
    val company: String,
    val platoon: String
) : Entity {

    override fun equals(other: Any?): Boolean = id == (other as? Recipient)?.id

    override fun hashCode(): Int = id.hashCode()
}
