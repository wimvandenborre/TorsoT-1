package com.personal;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.api.opensoundcontrol.OscAddressSpace;
import com.bitwig.extension.api.opensoundcontrol.OscConnection;
import com.bitwig.extension.api.opensoundcontrol.OscInvalidArgumentTypeException;
import com.bitwig.extension.api.opensoundcontrol.OscModule;
import com.bitwig.extension.api.opensoundcontrol.OscServer;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.CursorDevice;
import com.bitwig.extension.controller.api.CursorRemoteControlsPage;
import com.bitwig.extension.controller.api.NoteInput;
import com.bitwig.extension.controller.api.Preferences;
import com.bitwig.extension.controller.api.SettableBooleanValue;
import com.bitwig.extension.controller.api.SettableEnumValue;
import com.bitwig.extension.controller.api.SettableRangedValue;
import com.bitwig.extension.controller.api.SettableStringValue;
import com.bitwig.extension.controller.api.Signal;
import com.bitwig.extension.controller.api.Track;
import com.bitwig.extension.controller.api.TrackBank;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
   private static final String CONFIG_CATEGORY = "Config";
   private static final String CONFIG_DEFAULT_FILENAME = "TorsoT1-config.xml";
   private static final String OPTION_SELECTED_DEVICE = "Selected device on track";
   private static final String TRACK_REMOTE_PREFIX = "Track Remotes Page ";
   private static final String PROJECT_REMOTE_PREFIX = "Project Remotes Page ";
   private static final String OSC_CATEGORY = "OSC";
   private static final int OSC_PORT_MIN = 1;
   private static final int OSC_PORT_MAX = 65535;
   private static final int DEFAULT_OSC_IN_PORT = 9000;
   private static final int DEFAULT_OSC_OUT_PORT = 9001;
   private static final String DEFAULT_OSC_OUT_HOST = "127.0.0.1";
   private static final boolean DEFAULT_OSC_SERVER_ENABLED = true;

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
      mConfigPathSetting = preferences.getStringSetting(
         "Config file",
         CONFIG_CATEGORY,
         260,
         getDefaultConfigFile().getPath()
      );
      final String currentPath = mConfigPathSetting.get();
      final String legacyPath = new File(System.getProperty("user.home"), CONFIG_DEFAULT_FILENAME).getPath();
      if (currentPath == null || currentPath.trim().isEmpty() || currentPath.equals(legacyPath))
      {
         mConfigPathSetting.set(getDefaultConfigFile().getPath());
      }
      mSaveConfigSignal = preferences.getSignalSetting("Save config", CONFIG_CATEGORY, "Save");
      mLoadConfigSignal = preferences.getSignalSetting("Load config", CONFIG_CATEGORY, "Load");
      mSaveConfigSignal.addSignalObserver(this::saveConfig);
      mLoadConfigSignal.addSignalObserver(this::loadConfig);

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

      mOscModule = host.getOscModule();
      mOscAddressSpace = mOscModule.createAddressSpace();
      mOscAddressSpace.setName("TorsoT1");
      mOscAddressSpace.registerDefaultMethod((source, message) -> {
         if (DEBUG_MIDI)
         {
            host.println("TorsoT1 OSC recv: " + message.getAddressPattern());
         }
      });
      mOscServer = mOscModule.createUdpServer(mOscAddressSpace);
      mOscInPortSetting = preferences.getNumberSetting(
         "OSC In Port",
         OSC_CATEGORY,
         OSC_PORT_MIN,
         OSC_PORT_MAX,
         1,
         "",
         DEFAULT_OSC_IN_PORT
      );
      mOscOutPortSetting = preferences.getNumberSetting(
         "OSC Out Port",
         OSC_CATEGORY,
         OSC_PORT_MIN,
         OSC_PORT_MAX,
         1,
         "",
         DEFAULT_OSC_OUT_PORT
      );
      mOscOutHostSetting = preferences.getStringSetting(
         "OSC Out Host",
         OSC_CATEGORY,
         64,
         DEFAULT_OSC_OUT_HOST
      );
      mOscServerEnabledSetting = preferences.getBooleanSetting(
         "OSC Server Enabled",
         OSC_CATEGORY,
         DEFAULT_OSC_SERVER_ENABLED
      );
      mOscInPort = (int)Math.round(mOscInPortSetting.getRaw());
      if (mOscInPort <= 0)
      {
         mOscInPort = DEFAULT_OSC_IN_PORT;
      }
      mOscOutPort = (int)Math.round(mOscOutPortSetting.getRaw());
      if (mOscOutPort <= 0)
      {
         mOscOutPort = DEFAULT_OSC_OUT_PORT;
      }
      final String outHost = mOscOutHostSetting.get();
      mOscOutHost = (outHost == null || outHost.trim().isEmpty()) ? DEFAULT_OSC_OUT_HOST : outHost.trim();
      mOscServerEnabled = DEFAULT_OSC_SERVER_ENABLED;
      mOscInPortSetting.addRawValueObserver(value -> {
         mOscInPort = (int)Math.round(value);
         mOscServerAllowed = true;
         refreshOscServer();
      });
      mOscOutPortSetting.addRawValueObserver(value -> {
         mOscOutPort = (int)Math.round(value);
         requestOscReconnect();
      });
      mOscOutHostSetting.addValueObserver(value -> {
         mOscOutHost = value;
         requestOscReconnect();
      });
      mOscServerEnabledSetting.addValueObserver(value -> {
         mOscServerEnabled = value;
         refreshOscServer();
      });
      refreshOscConnection();
      mOscConnectAllowed = false;
      mOscServerAllowed = false;

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
/*          final String trackInfo = trackIndex >= 0 ? (" track " + (trackIndex + 1)) : "";
         getHost().println("TorsoT1: ch=" + (channel + 1) + " cc=" + cc + " val=" + value
            + " -> " + targetLabel + trackInfo + " remote " + (controlIndex + 1)
            + (CC_RELATIVE ? " (relative)" : " (absolute)")); */
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

      sendOscCombinedMessage(channel, cc, value / 127.0f, target, trackIndex);
   }

   private void sendOscCombinedMessage(final int channel, final int cc, final float value,
      final String target, final int trackIndex)
   {
      String targetType = "";
      int targetTrack = 0;
      int targetPage = 0;

      if (target == null)
      {
         sendOscMessage("/torsot1script/cc", channel + 1, cc, value, targetType, targetTrack, targetPage, "");
         return;
      }
      if (OPTION_SELECTED_DEVICE.equals(target))
      {
         if (trackIndex >= 0)
         {
            targetType = "selecteddevice";
            targetTrack = trackIndex + 1;
         }
      }
      else if (target.startsWith(TRACK_REMOTE_PREFIX))
      {
         final int pageIndex = parsePageIndex(target, TRACK_REMOTE_PREFIX);
         if (trackIndex >= 0 && pageIndex >= 0)
         {
            targetType = "trackremote";
            targetTrack = trackIndex + 1;
            targetPage = pageIndex + 1;
         }
      }
      else if (target.startsWith(PROJECT_REMOTE_PREFIX))
      {
         final int pageIndex = parsePageIndex(target, PROJECT_REMOTE_PREFIX);
         if (pageIndex >= 0)
         {
            targetType = "projectremote";
            targetPage = pageIndex + 1;
         }
      }

      final String combinedLabel = buildOscChannelLabel(channel, cc);
      sendOscMessage("/torsot1script/cc", channel + 1, cc, value, targetType, targetTrack, targetPage, combinedLabel);
   }

   private String buildOscChannelLabel(final int channel, final int cc)
   {
      return "Ch: " + (channel + 1) + " CC: " + cc;
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

   private boolean isValidTargetOption(final String target)
   {
      if (OPTION_SELECTED_DEVICE.equals(target))
      {
         return true;
      }
      if (target != null && target.startsWith(TRACK_REMOTE_PREFIX))
      {
         final int pageIndex = parsePageIndex(target, TRACK_REMOTE_PREFIX);
         return pageIndex >= 0 && pageIndex < REMOTE_PAGE_COUNT;
      }
      if (target != null && target.startsWith(PROJECT_REMOTE_PREFIX))
      {
         final int pageIndex = parsePageIndex(target, PROJECT_REMOTE_PREFIX);
         return pageIndex >= 0 && pageIndex < REMOTE_PAGE_COUNT;
      }
      return false;
   }

   private void saveConfig()
   {
      final String path = getConfigPath();
      try
      {
         final File file = new File(path);
         final File parent = file.getParentFile();
         if (parent != null && !parent.exists())
         {
            parent.mkdirs();
         }

         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder builder = factory.newDocumentBuilder();
         final Document doc = builder.newDocument();
         final Element root = doc.createElement("TorsoT1Config");
         root.setAttribute("version", "1");
         doc.appendChild(root);

         for (int i = 0; i < TRACK_COUNT; i++)
         {
            final Element channel = doc.createElement("Channel");
            channel.setAttribute("index", String.valueOf(i + 1));
            channel.setAttribute("btwTrk", String.valueOf(mChannelBtwTrkValues[i]));
            channel.setAttribute("target", mChannelTargetValues[i]);
            root.appendChild(channel);
         }

         final Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         transformer.transform(new DOMSource(doc), new StreamResult(file));
         getHost().showPopupNotification("TorsoT1: Config saved");
      }
      catch (Exception e)
      {
         getHost().errorln("TorsoT1: Failed to save config: " + e.getMessage());
         getHost().showPopupNotification("TorsoT1: Config save failed");
      }
   }

   private void loadConfig()
   {
      final String path = getConfigPath();
      final File file = new File(path);
      if (!file.exists())
      {
         getHost().showPopupNotification("TorsoT1: Config not found");
         return;
      }

      try
      {
         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder builder = factory.newDocumentBuilder();
         final Document doc = builder.parse(file);
         final NodeList channels = doc.getElementsByTagName("Channel");

         for (int i = 0; i < channels.getLength(); i++)
         {
            final Element element = (Element)channels.item(i);
            final int index = parsePositiveInt(element.getAttribute("index")) - 1;
            if (index < 0 || index >= TRACK_COUNT)
            {
               continue;
            }

            final int btwTrk = parsePositiveInt(element.getAttribute("btwTrk"));
            if (btwTrk >= 1 && btwTrk <= TRACK_PREF_MAX)
            {
               mChannelBtwTrkSettings[index].setRaw(btwTrk);
               mChannelBtwTrkValues[index] = btwTrk;
            }

            final String target = element.getAttribute("target");
            if (isValidTargetOption(target))
            {
               mChannelTargetSettings[index].set(target);
               mChannelTargetValues[index] = target;
            }
         }

         getHost().showPopupNotification("TorsoT1: Config loaded");
      }
      catch (Exception e)
      {
         getHost().errorln("TorsoT1: Failed to load config: " + e.getMessage());
         getHost().showPopupNotification("TorsoT1: Config load failed");
      }
   }

   private int parsePositiveInt(final String value)
   {
      try
      {
         final int parsed = Integer.parseInt(value);
         return Math.max(parsed, 0);
      }
      catch (NumberFormatException e)
      {
         return 0;
      }
   }

   private String getConfigPath()
   {
      final String path = mConfigPathSetting.get();
      if (path == null || path.trim().isEmpty())
      {
         return getDefaultConfigFile().getPath();
      }
      return path.trim();
   }

   private File getDefaultConfigFile()
   {
      final File documentsDir = new File(System.getProperty("user.home"), "Documents");
      final File bitwigDir = new File(documentsDir, "Bitwig Studio");
      final File extensionsDir = new File(bitwigDir, "Extensions");
      return new File(extensionsDir, CONFIG_DEFAULT_FILENAME);
   }

   private void refreshOscServer()
   {
      if (!mOscServerAllowed)
      {
         return;
      }
      if (mOscServer == null || mOscInPort <= 0 || !mOscServerEnabled)
      {
         return;
      }
      try
      {
         mOscServer.start(mOscInPort);
      }
      catch (IOException e)
      {
         getHost().errorln("TorsoT1: Failed to start OSC server on port " + mOscInPort + ": " + e.getMessage());
      }
   }

   private void refreshOscConnection()
   {
      if (!mOscConnectAllowed)
      {
         return;
      }
      if (mOscModule == null)
      {
         return;
      }
      final String host = mOscOutHost == null ? "" : mOscOutHost.trim();
      if (host.isEmpty() || mOscOutPort <= 0)
      {
         return;
      }
      try
      {
         mOscConnection = mOscModule.connectToUdpServer(host, mOscOutPort, null);
         if (DEBUG_MIDI)
         {
            getHost().println("TorsoT1: OSC connected to " + host + ":" + mOscOutPort);
         }
      }
      catch (Exception e)
      {
         getHost().errorln("TorsoT1: Failed to connect OSC to " + host + ":" + mOscOutPort + ": " + e.getMessage());
      }
   }

   private void requestOscReconnect()
   {
      if (mOscConnectAllowed)
      {
         refreshOscConnection();
      }
      else
      {
         getHost().println("TorsoT1: OSC Out settings changed; restart the extension to apply.");
      }
   }

   private void sendOscMessage(final String address, final Object... args)
   {
      if (mOscConnection == null)
      {
         return;
      }
      try
      {
         if (DEBUG_MIDI)
         {
            getHost().println("TorsoT1 OSC send: " + address + " -> " + mOscOutHost + ":" + mOscOutPort);
         }
         mOscConnection.sendMessage(address, args);
      }
      catch (IOException | OscInvalidArgumentTypeException e)
      {
         getHost().errorln("TorsoT1: Failed to send OSC " + address + ": " + e.getMessage());
      }
   }

   private Transport mTransport;
   private TrackBank mTrackBank;
   private CursorDevice[] mTrackDevices;
   private CursorRemoteControlsPage[] mTrackDeviceRemotes;
   private CursorRemoteControlsPage[] mTrackRemotePages;
   private CursorRemoteControlsPage mProjectRemotePage;
   private NoteInput mNoteInput;
   private SettableStringValue mConfigPathSetting;
   private Signal mSaveConfigSignal;
   private Signal mLoadConfigSignal;
   private SettableEnumValue[] mChannelTargetSettings;
   private SettableRangedValue[] mChannelBtwTrkSettings;
   private int[] mChannelBtwTrkValues;
   private String[] mChannelTargetValues;
   private OscModule mOscModule;
   private OscAddressSpace mOscAddressSpace;
   private OscServer mOscServer;
   private OscConnection mOscConnection;
   private SettableRangedValue mOscInPortSetting;
   private SettableRangedValue mOscOutPortSetting;
   private SettableStringValue mOscOutHostSetting;
   private SettableBooleanValue mOscServerEnabledSetting;
   private int mOscInPort;
   private int mOscOutPort;
   private String mOscOutHost;
   private boolean mOscServerEnabled;
   private boolean mOscServerAllowed = true;
   private boolean mOscConnectAllowed = true;
}
