package scrpgHelper.chargen

enum Energy:
    case Cold, Cosmic, Electricity, Fire, Infernal, Nuclear, Radiant, Sonic, Weather
end Energy

object Energy:
  def powerToEnergy(p: Power): Option[Energy] = p match
    case Power.cold => Some(Energy.Cold)
    case Power.cosmic => Some(Energy.Cosmic)
    case Power.electricity => Some(Energy.Electricity)
    case Power.fire => Some(Energy.Fire)
    case Power.infernal => Some(Energy.Infernal)
    case Power.nuclear => Some(Energy.Nuclear)
    case Power.radiant => Some(Energy.Radiant)
    case Power.sonic => Some(Energy.Sonic)
    case Power.weather => Some(Energy.Weather)
    case _ => None
end Energy
