package com.personal;
import java.util.UUID;

import com.bitwig.extension.api.PlatformType;
import com.bitwig.extension.controller.AutoDetectionMidiPortNamesList;
import com.bitwig.extension.controller.ControllerExtensionDefinition;
import com.bitwig.extension.controller.api.ControllerHost;

public class TorsoT1ExtensionDefinition extends ControllerExtensionDefinition
{
   private static final UUID DRIVER_ID = UUID.fromString("f4f4a9d2-8b0d-4db6-a2c4-4a5c0a3a5b55");
   
   public TorsoT1ExtensionDefinition()
   {
   }

   @Override
   public String getName()
   {
      return "T-1";
   }
   
   @Override
   public String getAuthor()
   {
      return "wimvandenborre";
   }

   @Override
   public String getVersion()
   {
      return "0.3";
   }

   @Override
   public UUID getId()
   {
      return DRIVER_ID;
   }
   
   @Override
   public String getHardwareVendor()
   {
      return "Per-Sonal";
   }
   
   @Override
   public String getHardwareModel()
   {
      return "T-1";
   }

   @Override
   public int getRequiredAPIVersion()
   {
      return 25;
   }

   @Override
   public int getNumMidiInPorts()
   {
      return 1;
   }

   @Override
   public int getNumMidiOutPorts()
   {
      return 1;
   }

   @Override
   public void listAutoDetectionMidiPortNames(final AutoDetectionMidiPortNamesList list, final PlatformType platformType)
   {
      if (platformType == PlatformType.WINDOWS)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0", "Input Port 1", "Input Port 2", "Input Port 3", "Input Port 4", "Input Port 5", "Input Port 6", "Input Port 7", "Input Port 8", "Input Port 9", "Input Port 10", "Input Port 11", "Input Port 12", "Input Port 13", "Input Port 14", "Input Port 15"}, new String[]{"Output Port 0", "Output Port 1", "Output Port 2", "Output Port 3", "Output Port 4", "Output Port 5", "Output Port 6", "Output Port 7", "Output Port 8", "Output Port 9", "Output Port 10", "Output Port 11", "Output Port 12", "Output Port 13", "Output Port 14", "Output Port 15"});
      }
      else if (platformType == PlatformType.MAC)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0", "Input Port 1", "Input Port 2", "Input Port 3", "Input Port 4", "Input Port 5", "Input Port 6", "Input Port 7", "Input Port 8", "Input Port 9", "Input Port 10", "Input Port 11", "Input Port 12", "Input Port 13", "Input Port 14", "Input Port 15"}, new String[]{"Output Port 0", "Output Port 1", "Output Port 2", "Output Port 3", "Output Port 4", "Output Port 5", "Output Port 6", "Output Port 7", "Output Port 8", "Output Port 9", "Output Port 10", "Output Port 11", "Output Port 12", "Output Port 13", "Output Port 14", "Output Port 15"});
      }
      else if (platformType == PlatformType.LINUX)
      {
         // TODO: Set the correct names of the ports for auto detection on Windows platform here
         // and uncomment this when port names are correct.
         // list.add(new String[]{"Input Port 0", "Input Port 1", "Input Port 2", "Input Port 3", "Input Port 4", "Input Port 5", "Input Port 6", "Input Port 7", "Input Port 8", "Input Port 9", "Input Port 10", "Input Port 11", "Input Port 12", "Input Port 13", "Input Port 14", "Input Port 15"}, new String[]{"Output Port 0", "Output Port 1", "Output Port 2", "Output Port 3", "Output Port 4", "Output Port 5", "Output Port 6", "Output Port 7", "Output Port 8", "Output Port 9", "Output Port 10", "Output Port 11", "Output Port 12", "Output Port 13", "Output Port 14", "Output Port 15"});
      }
   }

   @Override
   public TorsoT1Extension createInstance(final ControllerHost host)
   {
      return new TorsoT1Extension(this, host);
   }
}
