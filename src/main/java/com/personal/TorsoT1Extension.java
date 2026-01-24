package com.personal;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorDevice;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.NoteInput;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;

public class TorsoT1Extension extends ControllerExtension
{
   private static final int CC_START = 70;
   private static final int CC_END = 83;
   private static final int CC_COUNT = CC_END - CC_START + 1;
   private static final int TRACK_COUNT = 16;
   private static final boolean DEBUG_MIDI = true;
   private static final boolean CC_RELATIVE = false;

   protected TorsoT1Extension(final TorsoT1ExtensionDefinition definition, final ControllerHost host)
   {
      super(definition, host);
   }

   @Override
   public void init()
   {
      final ControllerHost host = getHost();      

      mTransport = host.createTransport();
      host.getMidiInPort(0).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi0(msg));
      host.getMidiInPort(0).setSysexCallback((String data) -> onSysex0(data));
      mNoteInput = host.getMidiInPort(0).createNoteInput("TorsoT1", "8?????", "9?????", "B?????");
      mNoteInput.setShouldConsumeEvents(false);

      mTrackBank = host.createMainTrackBank(TRACK_COUNT, 0, 0);
      mTrackDevices = new CursorDevice[TRACK_COUNT];
      mTrackRemotes = new CursorRemoteControlsPage[TRACK_COUNT];
      for (int i = 0; i < TRACK_COUNT; i++)
      {
         final Track track = mTrackBank.getItemAt(i);
         mTrackDevices[i] = track.createCursorDevice("TorsoT1 Track " + (i + 1));
         mTrackRemotes[i] = mTrackDevices[i].createCursorRemoteControlsPage(CC_COUNT);
      }

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("TorsoT1 Initialized");
      if (DEBUG_MIDI)
      {
         host.println("TorsoT1: MIDI debug enabled");
      }
   }

   @Override
   public void exit()
   {
      // TODO: Perform any cleanup once the driver exits
      // For now just show a popup notification for verification that it is no longer running.
      getHost().showPopupNotification("TorsoT1 Exited");
   }

   @Override
   public void flush()
   {
      // TODO Send any updates you need here.
   }

   /** Called when we receive short MIDI message on port 0. */
   private void onMidi0(ShortMidiMessage msg) 
   {
      handleCcToProjectRemotes(msg);
   }

   /** Called when we receive sysex MIDI message on port 0. */
   private void onSysex0(final String data) 
   {
      // MMC Transport Controls:
      if (data.equals("f07f7f0605f7"))
            mTransport.rewind();
      else if (data.equals("f07f7f0604f7"))
            mTransport.fastForward();
      else if (data.equals("f07f7f0601f7"))
            mTransport.stop();
      else if (data.equals("f07f7f0602f7"))
            mTransport.play();
      else if (data.equals("f07f7f0606f7"))
            mTransport.record();
   }
   private void handleCcToProjectRemotes(final ShortMidiMessage msg)
   {
      if (!msg.isControlChange())
      {
         return;
      }

      final int cc = msg.getData1();
      if (cc < CC_START || cc > CC_END)
      {
         return;
      }

      final int channel = msg.getChannel();
      if (channel < 0 || channel >= TRACK_COUNT)
      {
         return;
      }
      final int controlIndex = cc - CC_START;
      final int value = msg.getData2();
      if (DEBUG_MIDI)
      {
         getHost().println("TorsoT1: ch=" + (channel + 1) + " cc=" + cc + " val=" + value
            + " -> track " + (channel + 1) + " remote " + (controlIndex + 1)
            + (CC_RELATIVE ? " (relative)" : " (absolute)"));
      }

      if (CC_RELATIVE)
      {
         final int delta = value >= 64 ? value - 128 : value;
         if (delta != 0)
         {
            mTrackRemotes[channel].getParameter(controlIndex).inc(delta, 128);
         }
      }
      else
      {
         mTrackRemotes[channel].getParameter(controlIndex).set(value, 128);
      }
   }

   private Transport mTransport;
   private TrackBank mTrackBank;
   private CursorDevice[] mTrackDevices;
   private CursorRemoteControlsPage[] mTrackRemotes;
   private NoteInput mNoteInput;
}
