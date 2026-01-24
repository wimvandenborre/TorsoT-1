package com.personal;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorDevice;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.NoteInput;
import com.bitwig.extension.controller.api.Preferences;
import com.bitwig.extension.controller.api.SettableEnumValue;
import com.bitwig.extension.controller.api.SettableRangedValue;
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
   private static final int REMOTE_PAGE_COUNT = 8;
   private static final int TRACK_PREF_MAX = 128;
   private static final String OPTION_SELECTED_DEVICE = "Selected device on track";
   private static final String TRACK_REMOTE_PREFIX = "Track Remotes Page ";
   private static final String PROJECT_REMOTE_PREFIX = "Project Remotes Page ";

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

      final Preferences preferences = host.getPreferences();
      final String[] targetOptions = new String[1 + (REMOTE_PAGE_COUNT * 2)];
      targetOptions[0] = OPTION_SELECTED_DEVICE;
      for (int i = 0; i < REMOTE_PAGE_COUNT; i++)
      {
         targetOptions[i + 1] = TRACK_REMOTE_PREFIX + (i + 1);
         targetOptions[i + 1 + REMOTE_PAGE_COUNT] = PROJECT_REMOTE_PREFIX + (i + 1);
      }
      mChannelBtwTrkSettings = new SettableRangedValue[TRACK_COUNT];
      mChannelTargetSettings = new SettableEnumValue[TRACK_COUNT];
      mChannelBtwTrkValues = new int[TRACK_COUNT];
      mChannelTargetValues = new String[TRACK_COUNT];
      for (int i = 0; i < TRACK_COUNT; i++)
      {
         mChannelBtwTrkValues[i] = i + 1;
         mChannelBtwTrkSettings[i] = preferences.getNumberSetting(
            "Channel " + (i + 1) + " BtwTrk",
            "Channels",
            1,
            TRACK_PREF_MAX,
            1,
            "",
            i + 1
         );
         final int channelIndex = i;
         mChannelBtwTrkSettings[i].addRawValueObserver(value ->
            mChannelBtwTrkValues[channelIndex] = (int)Math.round(value));
      }
      for (int i = 0; i < TRACK_COUNT; i++)
      {
         mChannelTargetValues[i] = OPTION_SELECTED_DEVICE;
         final String name = "Channel " + (i + 1) + " Target";
         mChannelTargetSettings[i] = preferences.getEnumSetting(
            name,
            "Channels",
            targetOptions,
            OPTION_SELECTED_DEVICE
         );
         final int channelIndex = i;
         mChannelTargetSettings[i].addValueObserver(value ->
            mChannelTargetValues[channelIndex] = value);
      }

      mTrackBank = host.createMainTrackBank(TRACK_PREF_MAX, 0, 0);
      mTrackDevices = new CursorDevice[TRACK_PREF_MAX];
      mTrackDeviceRemotes = new CursorRemoteControlsPage[TRACK_PREF_MAX];
      mTrackRemotePages = new CursorRemoteControlsPage[TRACK_PREF_MAX];
      for (int i = 0; i < TRACK_PREF_MAX; i++)
      {
         final Track track = mTrackBank.getItemAt(i);
         mTrackDevices[i] = track.createCursorDevice("TorsoT1 Track " + (i + 1));
         mTrackDeviceRemotes[i] = mTrackDevices[i].createCursorRemoteControlsPage(CC_COUNT);
         mTrackRemotePages[i] = track.createCursorRemoteControlsPage(CC_COUNT);
      }
      mProjectRemotePage = host.getProject()
         .getRootTrackGroup()
         .createCursorRemoteControlsPage(CC_COUNT);

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
      handleCcToRemotes(msg);
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
   private void handleCcToRemotes(final ShortMidiMessage msg)
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

      final String target = mChannelTargetValues[channel];
      CursorRemoteControlsPage remotes = null;
      int trackIndex = -1;
      String targetLabel = target;
      if (OPTION_SELECTED_DEVICE.equals(target))
      {
         trackIndex = mChannelBtwTrkValues[channel] - 1;
         if (trackIndex < 0 || trackIndex >= TRACK_PREF_MAX)
         {
            return;
         }
         remotes = mTrackDeviceRemotes[trackIndex];
      }
      else if (target != null && target.startsWith(TRACK_REMOTE_PREFIX))
      {
         trackIndex = mChannelBtwTrkValues[channel] - 1;
         if (trackIndex < 0 || trackIndex >= TRACK_PREF_MAX)
         {
            return;
         }
         final int pageIndex = parsePageIndex(target, TRACK_REMOTE_PREFIX);
         if (pageIndex >= 0)
         {
            mTrackRemotePages[trackIndex].selectedPageIndex().set(pageIndex);
         }
         remotes = mTrackRemotePages[trackIndex];
      }
      else if (target != null && target.startsWith(PROJECT_REMOTE_PREFIX))
      {
         final int pageIndex = parsePageIndex(target, PROJECT_REMOTE_PREFIX);
         if (pageIndex >= 0)
         {
            mProjectRemotePage.selectedPageIndex().set(pageIndex);
         }
         remotes = mProjectRemotePage;
      }

      if (remotes == null)
      {
         return;
      }

      if (DEBUG_MIDI)
      {
         final String trackInfo = trackIndex >= 0 ? (" track " + (trackIndex + 1)) : "";
         getHost().println("TorsoT1: ch=" + (channel + 1) + " cc=" + cc + " val=" + value
            + " -> " + targetLabel + trackInfo + " remote " + (controlIndex + 1)
            + (CC_RELATIVE ? " (relative)" : " (absolute)"));
      }

      if (CC_RELATIVE)
      {
         final int delta = value >= 64 ? value - 128 : value;
         if (delta != 0)
         {
            remotes.getParameter(controlIndex).inc(delta, 128);
         }
      }
      else
      {
         remotes.getParameter(controlIndex).set(value, 128);
      }
   }

   private int parsePageIndex(final String target, final String prefix)
   {
      final String suffix = target.substring(prefix.length()).trim();
      try
      {
         final int page = Integer.parseInt(suffix);
         return page - 1;
      }
      catch (NumberFormatException e)
      {
         return -1;
      }
   }

   private Transport mTransport;
   private TrackBank mTrackBank;
   private CursorDevice[] mTrackDevices;
   private CursorRemoteControlsPage[] mTrackDeviceRemotes;
   private CursorRemoteControlsPage[] mTrackRemotePages;
   private CursorRemoteControlsPage mProjectRemotePage;
   private NoteInput mNoteInput;
   private SettableEnumValue[] mChannelTargetSettings;
   private SettableRangedValue[] mChannelBtwTrkSettings;
   private int[] mChannelBtwTrkValues;
   private String[] mChannelTargetValues;
}
