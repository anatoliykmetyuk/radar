// TODO
// Subscription -> Remove, Add to Channel, Enable/Disable
// Add to Channel -> List channels
// Remove -> Really?

// Channel -> Remove, Add Subscription, Enable/Disable
// Remove -> Really?
// Add Subscription -> List subscriptions


trait InterfaceWiring { this: ChatBot =>
  override def receive = {
    case Start(msg) =>
      // TODO check if user exists in the database. If not, add them.
      runR { for {
        user <- opt { msg.from }
        _    <- att { request(SendMessage(user.id, "Main Menu", replyMarkup = Some(mainMenu()))) }
      } yield () }

    case msg: TMessage =>
      runR { for {
        from <- opt { msg.from }
        s     = state.getOrElse(from.id, states.Empty)
        _    <- att { handleState(s, from.id)(msg) }
      } yield () }

    case RadarCommandWrapper(x, cbq) =>
      radarCommandsHandler(x, cbq)
      
  }

  def radarCommandsHandler(cmd: RadarCommand)(implicit cbq: CallbackQuery): Unit = cmd match {
    case MainMenu => msg("Main Menu", mainMenu())
    case Subscriptions =>
      val subs = List(Subscription(Some(1), 1, "fbevent"))
      msg("Your Subscriptions", subscriptions(subs))

    case Channels =>
      val chs = List(Channel(Some(1), "work", 1))
      msg("Your Channels", channels(chs))

    case CancelCmd =>
      state(cbq.from.id) = states.Empty
      msg("Menu", mainMenu())

    case Subscribe =>
      state(cbq.from.id) = states.Protocol
      msg("Please enter the protocol", cancel())

    case NewChannel =>
      state(cbq.from.id) = states.ChannelName
      msg("Please enter the name of the channel", cancel())

    case ChannelCmd(cid) =>
      msg(
        s"You are viewing channel with id $cid"
      , colI(
          "Main menu" -> MainMenu
        , "Back"      -> Channels
        )
      )

    case SubscriptionCmd(sid) =>
      msg(
        s"You are viewing subscriptions with id $sid"
      , colI(
          "Main menu" -> MainMenu
        , "Back"      -> Subscriptions
        )
      )
  }

  def handleState(s: states.DialogueState, from: Int)(implicit m: TMessage): Unit = {
    import states._
    s match {
      case Protocol =>
        state(from) = Target(m.text.get)
        msg(s"Got protocol: ${m.text.get}. Please enter target.", cancel())

      case Target(protocol) =>
        state(from) = Empty
        msg(s"Got target: ${m.text.get}. Subscribing you to ${protocol}:${m.text.get}")

      case ChannelName =>
        state(from) = Empty
        msg(s"Got channel name: ${m.text.get}")

      case Empty => log.info(s"Empty state message: $m")
    }
  }
}