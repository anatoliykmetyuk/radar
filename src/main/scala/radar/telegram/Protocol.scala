case class Start(msg: TMessage)
case class RadarCommandWrapper(cmd: RadarCommand, cbq: CallbackQuery)

sealed trait RadarCommand
case object Subscriptions extends RadarCommand
case object Channels      extends RadarCommand
case object MainMenu      extends RadarCommand
case object CancelCmd     extends RadarCommand

case object Subscribe     extends RadarCommand
case object NewChannel    extends RadarCommand

case class SubscriptionCmd(id: Int) extends RadarCommand
case class ChannelCmd     (id: Int) extends RadarCommand
