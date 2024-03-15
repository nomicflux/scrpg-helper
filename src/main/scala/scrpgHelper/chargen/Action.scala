package scrpgHelper.chargen

enum Action:
  case Attack, Defend, Hinder, Boost, Overcome, Recover

  def toSymbol: String = this match
    case Attack => "✊"
    case Defend => "🛡"
    case Hinder => "⩔"
    case Boost => "⩓"
    case Overcome => "↷"
    case Recover => "➕"
end Action
