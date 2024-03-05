package scrpgHelper.chargen

import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

enum PrincipleCategory:
    case Esoteric, Expertise, Ideals, Identity, Responsibility
end PrincipleCategory

case class Principle(
  name: Option[String],
  category: AbilityCategory,
  diePool: Set[EffectDieType],
  principleCategory: PrincipleCategory,
) extends Ability[Principle]:
    val status: Status = Status.Green
    val action: Action = Action.Overcome

    def changeName(s: String): Principle =
      copy(name = Some(s))
    end changeName
end Principle

object Principle:
    import PrincipleCategory.*

    def of(name: String, principleCategory: PrincipleCategory): Principle =
      new Principle(
        Some(name),
        AbilityCategory.Action,
        Set(EffectDieType.Max),
        principleCategory)
    end of

    def of(
      name: String,
      abilityCategory: AbilityCategory,
      effectDieType: EffectDieType,
      principleCategory: PrincipleCategory): Principle =
      new Principle(
        Some(name),
        abilityCategory,
        Set(effectDieType),
        principleCategory)
    end of

    val esotericPrinciples = List(
      Principle.of("Destiny", Esoteric),
      Principle.of("Cold", Esoteric),
      Principle.of("Cosmic Energy", Esoteric),
      Principle.of("Electricity", Esoteric),
      Principle.of("Fire", Esoteric),
      Principle.of("Infernal Energy", Esoteric),
      Principle.of("Nuclear Energy", Esoteric),
      Principle.of("Radiant Energy", Esoteric),
      Principle.of("Sonic Energy", Esoteric),
      Principle.of("Weather", Esoteric),
      Principle.of("Exorcism", Esoteric),
      Principle.of("Fauna", Esoteric),
      Principle.of("Flora", Esoteric),
      Principle.of("The Future", Esoteric),
      Principle.of("Immortality", Esoteric),
      Principle.of("The Inner Demon", Esoteric),
      Principle.of("Magic", Esoteric),
      Principle.of("The Sea", Esoteric),
      Principle.of("Space", Esoteric),
      Principle.of("Undead", Esoteric),
    )

    val expertisePrinciples = List(
      Principle.of("Clockwork", Expertise),
      Principle.of("The Gearhead", Expertise),
      Principle.of("History", Expertise),
      Principle.of("The Indestructible", Expertise),
      Principle.of("The Lab", Expertise),
      Principle.of("Mastery", Expertise),
      Principle.of("The Mentor", Expertise),
      Principle.of("The Powerless", Expertise),
      Principle.of("Science", Expertise),
      Principle.of("Speed", AbilityCategory.Inherent, EffectDieType.Mid, Expertise),
      Principle.of("Stealth", Expertise),
      Principle.of("Strength", Expertise),
      Principle.of("The Tactician", Expertise),
      Principle.of("Whispers", Expertise),
    )

    val idealsPrinciples = List(
      Principle.of("Chaos", Ideals),
      Principle.of("Compassion", Ideals),
      Principle.of("The Defender", Ideals),
      Principle.of("Dependence", Ideals),
      Principle.of("Equality", Ideals),
      Principle.of("Great Power", Ideals),
      Principle.of("The Hero", Ideals),
      Principle.of("Honor", Ideals),
      Principle.of("Justice", Ideals),
      Principle.of("Liberty", Ideals),
      Principle.of("Order", Ideals),
      Principle.of("Self Preservation", Ideals),
      Principle.of("The Zealot", Ideals),
    )

    val identityPrinciples = List(
      Principle.of("Ambition", Identity),
      Principle.of("Amnesia", Identity),
      Principle.of("Detachment", Identity),
      Principle.of("Discovery", Identity),
      Principle.of("Levity", Identity),
      Principle.of("The Loner", Identity),
      Principle.of("The Nomad", Identity),
      Principle.of("Peace", Identity),
      Principle.of("Rage", Identity),
      Principle.of("Savagery", Identity),
      Principle.of("The Split", Identity),
      Principle.of("The Spotless Mind", Identity),
    )

    val responsibilityPrinciples = List(
      Principle.of("Business", Responsibility),
      Principle.of("The Debtor", Responsibility),
      Principle.of("The Detective", Responsibility),
      Principle.of("The Double Agent", Responsibility),
      Principle.of("The Everyman", Responsibility),
      Principle.of("Family", Responsibility),
      Principle.of("The Mask", Responsibility),
      Principle.of("The Sidekick", Responsibility),
      Principle.of("The Team", Responsibility),
      Principle.of("The Underworld", Responsibility),
      Principle.of("The Veteran", Responsibility),
      Principle.of("Youth", Responsibility),
    )

    def categoryToPrinciples(category: PrincipleCategory): List[Principle] = category match
        case Esoteric => esotericPrinciples
        case Expertise => expertisePrinciples
        case Ideals => idealsPrinciples
        case Identity => identityPrinciples
        case Responsibility => responsibilityPrinciples
    end categoryToPrinciples
end Principle
