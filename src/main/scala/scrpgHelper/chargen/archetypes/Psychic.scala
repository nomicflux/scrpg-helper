package scrpgHelper.chargen.archetypes

object Psychic:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.Archetype.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val psychic = Archetype(
    "Psychic",
    12,
    ps => ps.filter(_.category == PowerCategory.Psychic).size >= 2,
    _ => true,
    0,
    Power.intellectualPowers ++ Power.materialPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    Quality.informationQualities ++ Quality.mentalQualities,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Psychic Assault",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
              ". Hinder the target using your Min die."
            )
          ),
          AbilityTemplate(
            "Psychic Coordination",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost using",
              PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
              ". Apply that bonus to all hero Attack and Overcome actions until the start of your next turn."
            )
          ),
          AbilityTemplate(
            "Psychic Insight",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After rolling during your turn, you may take 1 irreducible damage to reroll your entire dice pool."
            )
          ),
        )
      ),
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Astral Projection",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Overcome),
            List(
              "Overcome using",
              PowerChoice(AbilityChoice.onePower(Power.remoteViewing)),
              "and use your Max + Min dice. You do not have to be physically present in the area you are overcoming."
            )
          ),
          AbilityTemplate(
            "Illusionary Double",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When you are Attacked, Defend by rolling your single",
              PowerChoice(AbilityChoice.onePower(Power.illusions)),
              "die"
            )
          ),
          AbilityTemplate(
            "Minion Suggestion",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack a minion using",
              PowerChoice(AbilityChoice.onePower(Power.suggestion)),
              ". If that minion would be taken out, you control its next action, and then it is removed. Otherwise, Hinder it using your Min die."
            )
          ),
          AbilityTemplate(
            "Postcognitive Understanding",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Hinder),
            List(
              "After an enemy rolls dice to take an action for their turn but before using the result, Hinder that enemy's roll using your single",
              PowerChoice(AbilityChoice.onePower(Power.postcognition)),
              "die."
            )
          ),
          AbilityTemplate(
            "Precognitive Alteration",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            List(
              "After an ally rolls dice to take an action for their turn but before using the result, Boost that ally's roll using your single",
              PowerChoice(AbilityChoice.onePower(Power.precognition)),
              "die."
            )
          ),
          AbilityTemplate(
            "Psychic Analysis",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using",
              QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
              ". Either use your Max die, or use your Mid die and make it persistent and exclusive."
            )
          ),
          AbilityTemplate(
            "Swarm",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using",
              PowerChoice(AbilityChoice.onePower(Power.animalControl)),
              "and use your Min die."
            )
          ),
          AbilityTemplate(
            "Telekinetic Assault",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.onePower(Power.telekinesis)),
              ". Either Attack one target and use your Max die, or two targets and use your Mid die against one and your Min die against another."
            )
          ),
          AbilityTemplate(
            "Telepathic Whammy",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using",
              PowerChoice(AbilityChoice.onePower(Power.telepathy)),
              "and use your Max die. Hinder the target with a persistent and exclusive penalty using your Min die."
            )
          ),
        )
      )
    ),
    PrincipleCategory.Esoteric
  )
end Psychic
