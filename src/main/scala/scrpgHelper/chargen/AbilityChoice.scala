package scrpgHelper.chargen

import scala.reflect.TypeTest
import scrpgHelper.chargen.AbilityChoice.powerCategory

final class AbilityChoiceId

case class ChoiceContext(
  powers: List[Power],
  qualities: List[Quality],
  action: List[Action],
  energy: List[Energy]
)

trait AbilityChoice:
  type Item

  val id: AbilityChoiceId
  val getPower: Option[Power]
  val getQuality: Option[Quality]
  val getAction: Option[Action]
  val getEnergy: Option[Energy]

  def getItem: Option[Item]
  def itemName(i: Item): String
  def withChoice(i: Item): AbilityChoice
  def withoutChoice: AbilityChoice
  def validateFn: (List[Item], ChoiceContext) => Boolean

  def choiceName(default: String): String = getItem.fold(default)(itemName(_))
end AbilityChoice

object AbilityChoice:
  def noDupes[A](l: List[A], m: ChoiceContext): Boolean =
    l.distinct.size == l.size

  def nDistinct[A](n: Int)(l: List[A], m: ChoiceContext): Boolean =
    l.distinct.size >= (l.size - n + 1)

  def onePower(power: Power)(l: List[Power], m: ChoiceContext): Boolean =
    l.contains(power)

  def powerCategories(powerCategories: List[PowerCategory])(
      l: List[Power], m: ChoiceContext
  ): Boolean =
    !l.filter(p => powerCategories.contains(p.category)).isEmpty

  def powerCategory(powerCategory: PowerCategory)(l: List[Power], m: ChoiceContext): Boolean =
    powerCategories(List(powerCategory))(l, m)

  def intersection[A](toIntersect: List[A])(l: List[A], m: ChoiceContext): Boolean =
    !l.filter(a => toIntersect.toSet.contains(a)).isEmpty

  def qualityCategories(qualityCategories: List[QualityCategory])(
      l: List[Quality], m: ChoiceContext
  ): Boolean =
    !l.filter(q => qualityCategories.contains(q.category)).isEmpty

  def qualityCategory(qualityCategory: QualityCategory)(
      l: List[Quality], m: ChoiceContext
  ): Boolean =
    qualityCategories(List(qualityCategory))(l, m)

  def existingEnergyPower(l: List[Energy], m: ChoiceContext): Boolean =
    val energyPowers = m.powers.flatMap(p => Energy.powerToEnergy(p).toList).toSet
    !l.filter(e => energyPowers.contains(e)).isEmpty
  end existingEnergyPower

  def numChosen(l: List[AbilityChoice]): Int =
    l.map(ac =>
      ac.getPower
        .orElse(ac.getQuality)
        .orElse(ac.getAction)
        .orElse(ac.getEnergy)
    ).collect { case Some(_) => 1 }
      .sum
end AbilityChoice

case class PowerQualityChoice(
    id: AbilityChoiceId,
    powerQuality: Option[Power | Quality],
    validateFn: (List[Power | Quality], ChoiceContext) => Boolean
) extends AbilityChoice:
  type Item = Power | Quality
  val getPower: Option[Power] = powerQuality.flatMap { pq =>
    pq match
      case p: Power => Some(p)
      case _        => None
  }
  val getQuality: Option[Quality] = powerQuality.flatMap { pq =>
    pq match
      case q: Quality => Some(q)
      case _          => None
  }
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def withChoice(pq: Power | Quality): PowerQualityChoice =
    copy(powerQuality = Some(pq))
  def withoutChoice: PowerQualityChoice = copy(powerQuality = None)

  def getItem: Option[Item] = powerQuality
  def itemName(pq: Item): String = pq match
    case p: Power   => p.name
    case q: Quality => q.name

  override def toString(): String =
    s"PowerQualityChoice($powerQuality)"
end PowerQualityChoice

object PowerQualityChoice:
  def apply(): PowerQualityChoice =
    PowerQualityChoice(new AbilityChoiceId(), None, (_, _) => true)

  def apply(vfn: (List[Power | Quality], ChoiceContext) => Boolean): PowerQualityChoice =
    PowerQualityChoice(new AbilityChoiceId(), None, vfn)
end PowerQualityChoice

case class PowerChoice(
    id: AbilityChoiceId,
    power: Option[Power],
    validateFn: (List[Power], ChoiceContext) => Boolean
) extends AbilityChoice:
  type Item = Power
  val getPower: Option[Power] = power
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def withChoice(p: Power): PowerChoice = copy(power = Some(p))
  def withoutChoice: PowerChoice = copy(power = None)

  def getItem: Option[Item] = power
  def itemName(p: Power): String = p.name

  override def toString(): String =
    s"PowerChoice($power)"
end PowerChoice

object PowerChoice:
  def apply(): PowerChoice =
    PowerChoice(new AbilityChoiceId(), None, (_, _) => true)

  def apply(vfn: (List[Power], ChoiceContext) => Boolean): PowerChoice =
    PowerChoice(new AbilityChoiceId(), None, vfn)
end PowerChoice

case class QualityChoice(
    id: AbilityChoiceId,
    quality: Option[Quality],
    validateFn: (List[Quality], ChoiceContext) => Boolean
) extends AbilityChoice:
  type Item = Quality
  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = quality
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = None

  def withChoice(q: Quality): QualityChoice = copy(quality = Some(q))
  def withoutChoice: QualityChoice = copy(quality = None)

  def getItem: Option[Item] = quality
  def itemName(q: Quality): String = q.name

  override def toString(): String =
    s"QualityChoice($quality)"
end QualityChoice

object QualityChoice:
  def apply(): QualityChoice =
    QualityChoice(new AbilityChoiceId(), None, (_, _) => true)

  def apply(vfn: (List[Quality], ChoiceContext) => Boolean): QualityChoice =
    QualityChoice(new AbilityChoiceId(), None, vfn)
end QualityChoice

case class ActionChoice(
    id: AbilityChoiceId,
    action: Option[Action],
    validateFn: (List[Action], ChoiceContext) => Boolean
) extends AbilityChoice:
  type Item = Action

  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = action
  val getEnergy: Option[Energy] = None

  def withChoice(a: Action): ActionChoice = copy(action = Some(a))
  def withoutChoice: ActionChoice = copy(action = None)

  def getItem: Option[Item] = action
  def itemName(a: Item): String = a.toString

  override def toString(): String =
    s"ActionChoice($action)"
end ActionChoice

object ActionChoice:
  def apply(): ActionChoice =
    ActionChoice(new AbilityChoiceId(), None, (_, _) => true)

  def apply(vfn: (List[Action], ChoiceContext) => Boolean): ActionChoice =
    ActionChoice(new AbilityChoiceId(), None, vfn)
end ActionChoice

case class EnergyChoice(
    id: AbilityChoiceId,
    energy: Option[Energy],
    validateFn: (List[Energy], ChoiceContext) => Boolean,
    usePowersToValidate: Boolean
) extends AbilityChoice:
  type Item = Energy

  val getPower: Option[Power] = None
  val getQuality: Option[Quality] = None
  val getAction: Option[Action] = None
  val getEnergy: Option[Energy] = energy

  def withChoice(e: Energy): EnergyChoice = copy(energy = Some(e))
  def withoutChoice: EnergyChoice = copy(energy = None)

  def getItem: Option[Item] = energy
  def itemName(e: Item): String = e.toString

  override def toString(): String =
    s"EnergyChoice($energy)"
end EnergyChoice

object EnergyChoice:
  def apply(vfn: (List[Energy], ChoiceContext) => Boolean): EnergyChoice =
    EnergyChoice(new AbilityChoiceId(), None, vfn, false)

  def apply(): EnergyChoice =
    EnergyChoice((es, _) => !es.contains(Energy.Physical))

  def includePhysical(): EnergyChoice =
    EnergyChoice((_, _) => true)
end EnergyChoice
