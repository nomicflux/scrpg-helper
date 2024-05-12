package scrpgHelper.chargen.archetypes

object Flyer:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers = List(
    Power.signatureVehicle,
    Power.signatureWeapon,
  ) ++ Power.athleticPowers ++ Power.mobilityPowers ++ Power.technologicalPowers

  val qualities = Quality.informationQualities ++ Quality.physicalQualities

  val abilities = List(
          AbilityTemplate(
            "Aerial Bombardment",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack up to three targets using",
              PowerQualityChoice(AbilityChoice.noDupes),
              ". Attack using your Min die."
            )
          ),
          AbilityTemplate(
            "Aerial Surveillance",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost using",
              PowerQualityChoice(AbilityChoice.noDupes),
              ". Apply that bonus to all hero Attack and Overcome actions until the start of your next turn."
            )
          ),
          AbilityTemplate(
            "Barrel Roll",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When you are attacked while Flying, you may Defend yourself by rolling your single",
              PowerQualityChoice(AbilityChoice.noDupes),
              "die."
            )
          ),
          AbilityTemplate(
            "Dive & Drop",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack a minion using",
              PowerChoice(AbilityChoice.noDupes),
              ". Use whatever that minion rolls for its save as an Attack against another target of your choice."
            )
          ),
          AbilityTemplate(
            "Sonic Boom",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Hinder),
            List(
              "Hinder multiple targets using",
              PowerChoice(AbilityChoice.noDupes),
              ". Apply your Min die to each of them."
            )
          ),
          AbilityTemplate(
            "Strike & Swoop",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using",
              PowerQualityChoice(AbilityChoice.noDupes),
              ". Defend against all Attacks against you using your Min die until your next turn."
            )
          ),
        )

  val flyer = Archetype(
    "Flyer",
    8,
    ps => ps.contains(Power.flight) || ps.contains(Power.signatureVehicle),
    _ => true,
    1,
    powers,
    qualities,
    List(
      AbilityPool(
        2,
        abilities
      ),
      AbilityPool(
        1,
        abilities.map(_.copy(status = Status.Yellow))
      )
    ),
    PrincipleCategory.Ideals
  )
end Flyer
