package scrpgHelper.chargen

import scala.reflect.TypeTest

trait AbilityChoice:
  val getPower: Option[Power]
  val getQuality: Option[Quality]
  val getAction: Option[Action]
  val getEnergy: Option[Energy]

  def validate(l: List[AbilityChoice]): Boolean
end AbilityChoice

object AbilityChoice:
  def powerString(l: List[AbilityChoice]): String =
    l.collect { case pc: PowerChoice => pc.power.fold("<Power>")(_.name) }
      .reduceLeftOption(_ + "," + _).getOrElse("")

  def qualityString(l: List[AbilityChoice]): String =
    l.collect { case qc: QualityChoice =>
      qc.quality.fold("<Quality>")(_.name)
    }.reduceLeftOption(_ + "," + _).getOrElse("")

  def actionString(l: List[AbilityChoice]): String =
    l.collect { case ac: ActionChoice =>
      ac.action.fold("<Action>")(_.toString)
    }.reduceLeftOption(_ + "," + _).getOrElse("")

  def energyString(l: List[AbilityChoice]): String =
    l.collect { case ec: EnergyChoice =>
      ec.energy.fold("<Energy/Element>")(_.toString)
    }.reduceLeftOption(_ + "," + _).getOrElse("")
end AbilityChoice

case class PowerChoice(power: Option[Power], validateFn: List[Power] => Boolean)
    extends AbilityChoice:
  val getPower: Option[Power] = power
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case pc: PowerChoice => pc.power }.collect {
      case Some(p) => p
    })
end PowerChoice

object PowerChoice:
  def apply(): PowerChoice =
    PowerChoice(None, _ => true)

  def apply(vfn: List[Power] => Boolean): PowerChoice =
    PowerChoice(None, vfn)
end PowerChoice

case class QualityChoice(
    quality: Option[Quality],
    validateFn: List[Quality] => Boolean
) extends AbilityChoice:
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = quality
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case qc: QualityChoice => qc.quality }.collect {
      case Some(q) => q
    })
end QualityChoice

object QualityChoice:
  def apply(): QualityChoice =
    QualityChoice(None, _ => true)

  def apply(vfn: List[Quality] => Boolean): QualityChoice =
    QualityChoice(None, vfn)
end QualityChoice

case class ActionChoice(
    action: Option[Action],
    validateFn: List[Action] => Boolean
) extends AbilityChoice:
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = action
  val getEnergy: Option[Energy] = None

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case ac: ActionChoice => ac.action }.collect {
      case Some(a) => a
    })
end ActionChoice

object ActionChoice:
  def apply(): ActionChoice =
    ActionChoice(None, _ => true)

  def apply(vfn: List[Action] => Boolean): ActionChoice =
    ActionChoice(None, vfn)
end ActionChoice

case class EnergyChoice(
    energy: Option[Energy],
    validateFn: List[Energy] => Boolean
) extends AbilityChoice:
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = energy

  def validate(l: List[AbilityChoice]): Boolean =
    validateFn(l.collect { case ec: EnergyChoice => ec.energy }.collect {
      case Some(e) => e
    })
end EnergyChoice

object EnergyChoice:
  def apply(): EnergyChoice =
    EnergyChoice(None, _ => true)

  def apply(vfn: List[Energy] => Boolean): EnergyChoice =
    EnergyChoice(None, vfn)
end EnergyChoice
