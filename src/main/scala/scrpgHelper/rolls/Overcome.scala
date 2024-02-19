package scrpgHelper.rolls

enum Overcome:
  case SpectacularFailure, MajorTwist, MinorTwist, Success, BeyondExpectations

  def toDescription: String = this match
    case BeyondExpectations => "Beyond Expectations"
    case Success => "Success"
    case MinorTwist => "Minor Twist"
    case MajorTwist => "Major Twist / Failure"
    case SpectacularFailure => "Spectacular Failure"
  end toDescription

  def toClassName: String = this match
    case BeyondExpectations => "beyond-expectations"
    case Success => "success"
    case MinorTwist => "minor-twist"
    case MajorTwist => "major-twist"
    case SpectacularFailure => "spectacular-failure"
  end toClassName
end Overcome

object Overcome:
  def fromNumber(n: Int): Overcome =
    if(n >= 12) {
      BeyondExpectations
    } else if(n >= 8) {
      Success
    } else if(n >= 4) {
      MinorTwist
    } else if(n >= 1) {
      MajorTwist
    } else {
      SpectacularFailure
    }
  end fromNumber
end Overcome
