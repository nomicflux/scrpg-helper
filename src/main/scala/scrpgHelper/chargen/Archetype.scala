package scrpgHelper.chargen

case class Archetype(
    name: String,
    number: Int,
    powerValidation: List[Power] => Boolean,
    qualityValidation: List[Quality] => Boolean,
    minPowers: Int,
    powerList: List[Power],
    qualityList: List[Quality],
    abilityPools: List[AbilityPool],
    principleCategory: PrincipleCategory
):
  def valid(): Boolean = false
end Archetype

object Archetype:
  import scrpgHelper.chargen.archetypes.*

  def signaturePower(p: Power): List[Power] => Boolean =
    ps => ps.contains(p)

  def signatureQuality(q: Quality): List[Quality] => Boolean =
    qs => qs.contains(q)

  val archetypes: List[Archetype] = List(
    Speedster.speedster
  )
end Archetype
