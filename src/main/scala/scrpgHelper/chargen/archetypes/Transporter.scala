package scrpgHelper.chargen.archetypes

object Transporter:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers = List(
    Power.signatureVehicle,
  ) ++ Power.athleticPowers ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.technologicalPowers

  val qualities = Quality.physicalQualities ++ Quality.socialQualities

  val abilities = List(
          AbilityTemplate(
            "Displacement Assault",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Either Hinder your target with your Min die or move them somewhere else in the scene."
            )
          ),
          AbilityTemplate(
            "Hit & Run",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Defend),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Defend against all Attacks against you using your Min die until your next turn."
            )
          ),
          AbilityTemplate(
            "Mobile Assist",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Attack),
            List(
              "Boost another hero using using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Attack using your Min die."
            )
          ),
          AbilityTemplate(
            "Mobile Dodge",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Attack),
            List(
              "When you are hit with an Attack, you may take 1 irreducible damage to have the attacker reroll their dice pool."
            )
          ),
          AbilityTemplate(
            "Run Down",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Min die against each."
            )
          ),
        )

  val transporter = Archetype(
    "Transporter",
    13,
    ps => ps.contains(Power.signatureVehicle) || !ps.filter(_.category == PowerCategory.Mobility).isEmpty,
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
    PrincipleCategory.Expertise
  )
end Transporter
