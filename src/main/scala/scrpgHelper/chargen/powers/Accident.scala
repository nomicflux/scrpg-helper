package scrpgHelper.chargen.powers

object Accident:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  def accident(using
      acAP: AbilityChoice[(Action, Power)],
      acP: AbilityChoice[Power]
  ) = PowerSource(
    "Accident",
    1,
    List(
      AbilityPool(
        TextAbility[(Action, Power)](
          "Area Alteration",
          Status.Yellow,
          AbilityCategory.Action,
          choice => choice.getAction.toList,
          choice =>
            s"${choice.actionString} any number of nearby targets using ${choice.powerString}. Use your Max die.",
          (choice, ability, abilities) =>
            Set(Action.Boost, Action.Hinder).contains(choice.getAction)
              && statusValidation(2, Status.Yellow, abilities :+ ability)
        ),
        TextAbility[Power](
          "Inflict",
          Status.Yellow,
          AbilityCategory.Action,
          _ => List(Action.Attack, Action.Hinder),
          choice =>
            s"Attack using ${choice.powerString}. Hinder that same target using your Min die.",
          (power, ability, abilities) =>
            statusValidation(2, Status.Yellow, abilities :+ ability)
        ),
        TextAbility[Power](
          "Reflexive Burst",
          Status.Yellow,
          AbilityCategory.Reaction,
          _ => List(Action.Attack),
          choice =>
            s"When your personal zone changes, Attack all close enemy targets by rolling your single ${choice.powerString} die.",
          (power, ability, abilities) =>
            statusValidation(2, Status.Yellow, abilities :+ ability)
        ),
        TextAbility[Power](
          "Ambush Awareness",
          Status.Green,
          AbilityCategory.Reaction,
          _ => List(Action.Defend),
          choice =>
            s"If you haven't yet acted in an action scene, you may Defend against an Attack by rolling your single ${choice.powerString} die.",
          (power, ability, abilities) =>
            statusValidation(1, Status.Green, abilities :+ ability)
        ),
        TextAbility[Power](
          "Change in Circumstance",
          Status.Green,
          AbilityCategory.Reaction,
          _ => List(Action.Boost),
          choice =>
            s"When you change personal zones, you may Boost by rolling your single ${choice.powerString} die.",
          (power, ability, abilities) =>
            statusValidation(1, Status.Green, abilities :+ ability)
        ),
        TextAbility[Power](
          "Immunity",
          Status.Green,
          AbilityCategory.Inherent,
          _ => List(),
          choice => s"You do not take damage from ${choice.powerString}",
          (power, ability, abilities) =>
            Set(Power.energyPowers).contains(power)
              && statusValidation(1, Status.Green, abilities :+ ability)
        )
      )
    ),
    Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.materialPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(12), d(6))
  )
end Accident
