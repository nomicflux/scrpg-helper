package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Signal manager interface - reactive signal infrastructure.
  *
  * This trait captures any remaining signals and reactive infrastructure
  * that don't fit into the other categories. Currently empty as all
  * signals have been captured in the other traits.
  */
trait SignalManager:

  // All signals are captured in other traits
  // This interface is reserved for any additional signal management needs

end SignalManager