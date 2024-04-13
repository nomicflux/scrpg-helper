package scrpgHelper.chargen.powers

object Multiverse:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val multiverse: PowerSource = PowerSource(
    "The Multiverse",
    20,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Power From Beyond",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Attack),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Max die. That bonus is persistent and exclusive. Then, Attack using your Min die."
            ),
          ),
          AbilityTemplate(
            "Respond in Kind",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "When your are hit with an Attack at close range, the attacker also takes damage equal to their effect die.",
            ),
          ),
          AbilityTemplate(
            "Dread Pallor",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Hinder),
            List(
              "Hinder multiple targets using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Mid die for one and your Min die for the rest.",
            ),
          ),
          AbilityTemplate(
            "Reality Scorned",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack uising ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". If your target survived, Hinder them using your Max die.",
            ),
          ),
        ),
      ),
    ),
    List(Power.awareness, Power.cosmic, Power.intuition, Power.speed, Power.teleportation) ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(10), d(8), d(6)),
    None,
    Some((d(6), Power.allPowers))
  )
end Multiverse
