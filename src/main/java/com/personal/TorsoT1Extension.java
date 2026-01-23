package com.personal;

import com.bitwig.extension.api.util.midi.ShortMidiMessage;
import com.bitwig.extension.callback.ShortMidiMessageReceivedCallback;
import com.bitwig.extension.controller.api.ControllerHost;
import com.bitwig.extension.controller.api.Transport;
import com.bitwig.extension.controller.ControllerExtension;

public class TorsoT1Extension extends ControllerExtension
{
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
      host.getMidiInPort(1).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi1(msg));
      host.getMidiInPort(1).setSysexCallback((String data) -> onSysex1(data));
      host.getMidiInPort(2).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi2(msg));
      host.getMidiInPort(2).setSysexCallback((String data) -> onSysex2(data));
      host.getMidiInPort(3).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi3(msg));
      host.getMidiInPort(3).setSysexCallback((String data) -> onSysex3(data));
      host.getMidiInPort(4).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi4(msg));
      host.getMidiInPort(4).setSysexCallback((String data) -> onSysex4(data));
      host.getMidiInPort(5).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi5(msg));
      host.getMidiInPort(5).setSysexCallback((String data) -> onSysex5(data));
      host.getMidiInPort(6).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi6(msg));
      host.getMidiInPort(6).setSysexCallback((String data) -> onSysex6(data));
      host.getMidiInPort(7).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi7(msg));
      host.getMidiInPort(7).setSysexCallback((String data) -> onSysex7(data));
      host.getMidiInPort(8).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi8(msg));
      host.getMidiInPort(8).setSysexCallback((String data) -> onSysex8(data));
      host.getMidiInPort(9).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi9(msg));
      host.getMidiInPort(9).setSysexCallback((String data) -> onSysex9(data));
      host.getMidiInPort(10).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi10(msg));
      host.getMidiInPort(10).setSysexCallback((String data) -> onSysex10(data));
      host.getMidiInPort(11).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi11(msg));
      host.getMidiInPort(11).setSysexCallback((String data) -> onSysex11(data));
      host.getMidiInPort(12).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi12(msg));
      host.getMidiInPort(12).setSysexCallback((String data) -> onSysex12(data));
      host.getMidiInPort(13).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi13(msg));
      host.getMidiInPort(13).setSysexCallback((String data) -> onSysex13(data));
      host.getMidiInPort(14).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi14(msg));
      host.getMidiInPort(14).setSysexCallback((String data) -> onSysex14(data));
      host.getMidiInPort(15).setMidiCallback((ShortMidiMessageReceivedCallback)msg -> onMidi15(msg));
      host.getMidiInPort(15).setSysexCallback((String data) -> onSysex15(data));

      // TODO: Perform your driver initialization here.
      // For now just show a popup notification for verification that it is running.
      host.showPopupNotification("TorsoT1 Initialized");
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
      // TODO: Implement your MIDI input handling code here.
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
   /** Called when we receive short MIDI message on port 1. */
   private void onMidi1(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 1. */
   private void onSysex1(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 2. */
   private void onMidi2(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 2. */
   private void onSysex2(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 3. */
   private void onMidi3(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 3. */
   private void onSysex3(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 4. */
   private void onMidi4(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 4. */
   private void onSysex4(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 5. */
   private void onMidi5(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 5. */
   private void onSysex5(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 6. */
   private void onMidi6(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 6. */
   private void onSysex6(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 7. */
   private void onMidi7(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 7. */
   private void onSysex7(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 8. */
   private void onMidi8(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 8. */
   private void onSysex8(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 9. */
   private void onMidi9(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 9. */
   private void onSysex9(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 10. */
   private void onMidi10(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 10. */
   private void onSysex10(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 11. */
   private void onMidi11(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 11. */
   private void onSysex11(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 12. */
   private void onMidi12(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 12. */
   private void onSysex12(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 13. */
   private void onMidi13(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 13. */
   private void onSysex13(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 14. */
   private void onMidi14(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 14. */
   private void onSysex14(final String data) 
   {
   }
   /** Called when we receive short MIDI message on port 15. */
   private void onMidi15(ShortMidiMessage msg) 
   {
      // TODO: Implement your MIDI input handling code here.
   }

   /** Called when we receive sysex MIDI message on port 15. */
   private void onSysex15(final String data) 
   {
   }

   private Transport mTransport;
}
