package scrpgHelper.chargen

import scala.reflect.TypeTest

final class AbilityChoiceId

trait AbilityChoice:
  val id: AbilityChoiceId
  val getPower: Option[Power]
  val getQuality: Option[Quality]
  val getAction: Option[Action]
  val getEnergy: Option[Energy]

  def withoutChoice: AbilityChoice
  def validate(l: List[AbilityChoice]): Boolean
end AbilityChoice

object AbilityChoice:
  def noDupes[A](l: List[A]): Boolean =
      l.distinct.size == l.size

  def numChosen(l: List[AbilityChoice]): Int =
    l.map(ac => ac.getPower.orElse(ac.getQuality).orElse(ac.getAction).orElse(ac.getEnergy)).collect { case Some(_) => 1 }.sum

  def powerChoices(l: List[AbilityChoice]): List[PowerChoice] =
    l.collect { case pc: PowerChoice => pc }

  def powerString(l: List[AbilityChoice]): String =
    l.collect { case pc: PowerChoice => pc.power.fold("<Power>")(_.name) }
      .reduceLeftOption(_ + "," + _).getOrElse("")

  def qualityChoices(l: List[AbilityChoice]): List[QualityChoice] =
    l.collect { case qc: QualityChoice => qc }

  def qualityString(l: List[AbilityChoice]): String =
    l.collect { case qc: QualityChoice =>
      qc.quality.fold("<Quality>")(_.name)
    }.reduceLeftOption(_ + "," + _).getOrElse("")

  def actionChoices(l: List[AbilityChoice]): List[ActionChoice] =
    l.collect { case ac: ActionChoice => ac }

  def actionString(l: List[AbilityChoice]): String =
    l.collect { case ac: ActionChoice =>
      ac.action.fold("<Action>")(_.toString)
    }.reduceLeftOption(_ + "," + _).getOrElse("")

  def energyChoices(l: List[AbilityChoice]): List[EnergyChoice] =
    l.collect { case ec: EnergyChoice => ec }

  def energyString(l: List[AbilityChoice]): String =
    l.collect { case ec: EnergyChoice =>
      ec.energy.fold("<Energy/Element>")(_.toString)
    }.reduceLeftOption(_ + "," + _).getOrElse("")
end AbilityChoice

case class PowerChoice(id: AbilityChoiceId, power: Option[Power], validateFn: List[Power] => Boolean)
    extends AbilityChoice:
  val getPower: Option[Power] = power
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def withChoice(p: Power): PowerChoice = copy(power = Some(p))
  def withoutChoice: PowerChoice = copy(power = None)

  override def toString(): String =
    s"PowerChoice($power)"

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case pc: PowerChoice => pc.power }.collect {
      case Some(p) => p
    })
end PowerChoice

object PowerChoice:
  def apply(): PowerChoice =
    PowerChoice(new AbilityChoiceId(), None, _ => true)

  def apply(vfn: List[Power] => Boolean): PowerChoice =
    PowerChoice(new AbilityChoiceId(), None, vfn)
end PowerChoice

case class QualityChoice(
    id: AbilityChoiceId,
    quality: Option[Quality],
    validateFn: List[Quality] => Boolean
) extends AbilityChoice:
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = quality
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def withChoice(q: Quality): QualityChoice = copy(quality = Some(q))
  def withoutChoice: QualityChoice = copy(quality = None)

  override def toString(): String =
    s"QualityChoice($quality)"

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case qc: QualityChoice => qc.quality }.collect {
      case Some(q) => q
    })
end QualityChoice

object QualityChoice:
  def apply(): QualityChoice =
    QualityChoice(new AbilityChoiceId(), None, _ => true)

  def apply(vfn: List[Quality] => Boolean): QualityChoice =
    QualityChoice(new AbilityChoiceId(), None, vfn)
end QualityChoice

case class ActionChoice(
    id: AbilityChoiceId,
    action: Option[Action],
    validateFn: List[Action] => Boolean
) extends AbilityChoice:
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = action
  val getEnergy: Option[Energy] = None

  def withChoice(a: Action): ActionChoice = copy(action = Some(a))
  def withoutChoice: ActionChoice = copy(action = None)

  override def toString(): String =
    s"ActionChoice($action)"

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case ac: ActionChoice => ac.action }.collect {
      case Some(a) => a
    })
end ActionChoice

object ActionChoice:
  def apply(): ActionChoice =
    ActionChoice(new AbilityChoiceId(), None, _ => true)

  def apply(vfn: List[Action] => Boolean): ActionChoice =
    ActionChoice(new AbilityChoiceId(), None, vfn)
end ActionChoice

case class EnergyChoice(
    id: AbilityChoiceId,
    energy: Option[Energy],
    validateFn: List[Energy] => Boolean
) extends AbilityChoice:
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = energy

  def withChoice(e: Energy): EnergyChoice = copy(energy = Some(e))
  def withoutChoice: EnergyChoice = copy(energy = None)

  override def toString(): String =
    s"EnergyChoice($energy)"

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case ec: EnergyChoice => ec.energy }.collect {
      case Some(e) => e
    })
end EnergyChoice

object EnergyChoice:
  def apply(): EnergyChoice =
    EnergyChoice(new AbilityChoiceId(), None, _ => true)

  def apply(vfn: List[Energy] => Boolean): EnergyChoice =
    EnergyChoice(new AbilityChoiceId(), None, vfn)
end EnergyChoice
