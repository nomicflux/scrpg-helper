package scrpgHelper.chargen.powers

object Alien:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val powers =
    List(Power.awareness, Power.cold, Power.electricity, Power.fire, Power.infernal, Power.plants, Power.presence, Power.radiant, Power.strength, Power.transmutation, Power.vitality, Power.weather) ++ Power.mobilityPowers ++ Power.psychicPowers ++ Power.selfControlPowers

  val supernatural: PowerSource = PowerSource(
    "Supernatural",
    11,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Area Healing",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Recover),
            List(
              "Boost an ally using ",
              PowerChoice(AbilityChoice.noDupes),
              ". You and nearby heroes in the Yellow and Red zones Recover Health equal to your Min die."
            ),
          ),
          AbilityTemplate(
            "Mass Modification",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost or Hinder using ",
              PowerChoice(AbilityChoice.noDupes),
              ", and apply that mod to multiple close targets.",
            ),
          ),
          AbilityTemplate(
            "Personal Upgrade",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max die. That bonus is persistent and exclusive.",
            ),
          ),
          AbilityTemplate(
            "Reach through Veil",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When a nearby ally would take damage, Defend that ally by rolling your single status die, and move them elsewhere in the same scene.",
            ),
          ),
        ),
      ),
    ),
    powers,
    List(d(10), d(10), d(6)),
    None,
    Some((d(10), Power.allPowers.filterNot(p => powers.toSet.contains(p)))),
  )
end Alien
