package scrpgHelper.status

enum Status:
    case Green, Yellow, Red, Out

    def >(status: Status): Boolean =
      this.ordinal > status.ordinal
    end >

    def >=(status: Status): Boolean =
      this.ordinal >= status.ordinal
    end >=

    def <(status: Status): Boolean =
      this.ordinal < status.ordinal
    end <

    def <=(status: Status): Boolean =
      this.ordinal <= status.ordinal
    end <=
end Status
