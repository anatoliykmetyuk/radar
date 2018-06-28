package radar

object time extends TimeTrait
trait TimeTrait {
  lazy val day    = 24 * hours
  lazy val hour   = 60 * minutes
  lazy val minute = 60 * seconds
  lazy val second = 1000
  
  lazy val days    = day
  lazy val hours   = hour
  lazy val minutes = minute
  lazy val seconds = second

  def now = System.currentTimeMillis

  def roundDay(t: Long): Long =
    t / day * day
}
