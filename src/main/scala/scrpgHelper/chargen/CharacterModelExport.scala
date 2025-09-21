package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Structured export data for a complete character.
  *
  * This case class contains all the essential character information
  * in a serializable format for export and persistence.
  *
  * @param background Character's background name
  * @param powerSource Character's power source name
  * @param archetype Character's archetype name
  * @param personality Character's personality name
  * @param statuses Map of status types to their die values
  * @param health Character's health value
  * @param powers List of powers with their dice
  * @param qualities List of qualities with their dice
  * @param abilities List of chosen abilities with configurations
  * @param principles List of principles
  */
case class CharacterModelExport(
    background: Option[String],
    powerSource: Option[String],
    archetype: Option[String],
    personality: Option[String],
    statuses: Map[Status, Die],
    health: Option[Int],
    powers: List[(Power, Die)],
    qualities: List[(Quality, Die)],
    abilities: List[ChosenAbility],
    principles: List[Principle]
):

  /** Render the character export as formatted text.
    *
    * @return A multi-line string representation of the character
    */
  def render: String =
    val healthMarks = health.map(Health.calcRanges(_))
    val powerText = powers
      .map((p, d) => s"* ${p.name}, ${p.category.toString} - ${d.toString}")
      .mkString("\n\t")
    val qualityText = qualities
      .map((q, d) => s"* ${q.name}, ${q.category.toString} - ${d.toString}")
      .mkString("\n\t")
    val statusText = statuses.toList
      .sortBy(_._1)
      .map((s, d) => s"* ${s.toString}: ${d.toString}")
      .mkString("\n\t")
    val principleText = principles
      .map(p => "* " + RenderAbility.renderPrincipleText(p))
      .mkString("\n\t")
    val abilityText = abilities
      .sortBy(_.status)
      .map(ca => "* " + RenderAbility.renderChosenAbilityText(ca))
      .mkString("\n\t")
    s"""
Background: ${background.getOrElse("<none>")}
Power Source: ${powerSource.getOrElse("<none>")}
Archetype: ${archetype.getOrElse("<none>")}
Personality: ${personality.getOrElse("<none>")}
Health: ${healthMarks.fold("")(hm =>
        hm._1.toString + " - " + (hm._2 + 1).toString + "; " + hm._2.toString + " - " + (hm._3 + 1).toString + "; " + hm._3 + " - 1"
      )}
Powers:
\t${powerText}
Qualities:
\t${qualityText}
Status Dice:
\t${statusText}
Principles:
\t${principleText}
Abilities:
\t${abilityText}
       """
  end render

end CharacterModelExport