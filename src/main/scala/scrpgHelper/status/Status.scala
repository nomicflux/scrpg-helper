package scrpgHelper.status

enum Status extends Ordered[Status]:
    case Green, Yellow, Red, Out

    def compare(that: Status): Int =
      this.ordinal - that.ordinal
end Status
