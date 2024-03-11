package scrpgHelper.chargen

import scala.reflect.TypeTest

trait AbilityChoice[A]:
  extension (a: A) def getPower: Option[Power]
  extension (a: A) def getQuality: Option[Quality]
  extension (a: A) def getAction: Option[Action]

  extension (a: A) def powerString: String = getPower(a).fold("<none chosen>")(_.name)
  extension (a: A) def qualityString: String = getQuality(a).fold("<none chosen>")(_.name)
  extension (a: A) def actionString: String = getAction(a).fold("<none chosen>")(_.toString)
end AbilityChoice

object AbilityChoice:
  given AbilityChoice[Power] with
    extension (power: Power) def getPower: Option[Power] = Some(power)
    extension (power: Power) def getQuality: Option[Quality] = None
    extension (power: Power) def getAction: Option[Action] = None

  given AbilityChoice[Quality] with
    extension (quality: Quality) def getPower: Option[Power] = None
    extension (quality: Quality) def getQuality: Option[Quality] = Some(quality)
    extension (quality: Quality) def getAction: Option[Action] = None

  given AbilityChoice[Action] with
    extension (action: Action) def getPower: Option[Power] = None
    extension (action: Action) def getQuality: Option[Quality] = None
    extension (action: Action) def getAction: Option[Action] = Some(action)

  given [A, B](using
      acA: AbilityChoice[A],
      acB: AbilityChoice[B]
  ): AbilityChoice[(A, B)] with
    extension (ab: (A, B))
      def getPower: Option[Power] = ab._1.getPower.orElse(ab._2.getPower)
    extension (ab: (A, B))
      def getQuality: Option[Quality] =
        ab._1.getQuality.orElse(ab._2.getQuality)
    extension (ab: (A, B))
      def getAction: Option[Action] = ab._1.getAction.orElse(ab._2.getAction)

  given [A, B](using
      acA: AbilityChoice[A],
      acB: AbilityChoice[B],
      ttA: TypeTest[A | B, A],
      ttB: TypeTest[A | B, B]
  ): AbilityChoice[A | B] with
    extension (ab: A | B)
      def getPower: Option[Power] = ab match
        case ttA(a) => a.getPower
        case ttB(b) => b.getPower
    extension (ab: A | B)
      def getQuality: Option[Quality] = ab match
        case ttA(a) => a.getQuality
        case ttB(b) => b.getQuality
    extension (ab: A | B)
      def getAction: Option[Action] = ab match
        case ttA(a) => a.getAction
        case ttB(b) => b.getAction
end AbilityChoice
