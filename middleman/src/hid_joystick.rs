use std::path::Path;


pub struct HidController {

}

impl HidController {
    pub fn new<P: AsRef<Path>>(_p: P) -> std::io::Result<HidController> {

        Ok(HidController { 

        })
    }

    // ref: 
    // https://www.usb.org/sites/default/files/hid1_11.pdf
}

mod tables {
    macro_rules! usage_table {
        ([$strct:ident] $($id:literal => $name:ident),* $(,)?) => {
            pub enum $strct {
                $(
                    $name
                ),*
            }

            impl Into<u8> for $strct {
                fn into(self) -> u8 {
                    match self {
                        $(
                            $strct::$name => $id
                        ),*
                    }
                }
            }

            impl TryFrom<u8> for $strct {
                type Error = ();

                fn try_from(value: u8) -> Result<Self, Self::Error> {
                    match value {
                        $(
                            $id => Ok($strct::$name),
                        )*
                        _ => Err(())
                    }
                }
            }
        };
    }

    // Translated from https://www.usb.org/sites/default/files/hut1_4.pdf
    usage_table! {
        [UsageTable]
        0x01 => Pointer,
        0x02 => Mouse,
        0x04 => Joystick,
        0x05 => Gamepad,
        0x06 => Keyboard,
        0x07 => Keypad,
        0x08 => MultiAxisController,
        0x09 => TabletPCSystemControls,
        0x0a => WaterCoolingDevice,
        0x0b => ComputerChassisDevice,
        0x0c => WirelessRadioControls,
        0x0d => PortableDeviceControl,
        0x0e => SystemMultiAxisController,
        0x0f => SpatialController,
        0x10 => AssistiveControl,
        0x11 => DeviceDock,
        0x12 => DockableDevice,
        0x13 => CallStateManagementControl,
        0x30 => X,
        0x31 => Y,
        0x32 => Z,
        0x33 => Rx,
        0x34 => Ry,
        0x35 => Rz,
        0x36 => Slider,
        0x37 => Dial,
        0x38 => Wheel,
        0x39 => HatSwitch,
        0x3a => CountedBuffer,
        0x3b => ByteCount,
        0x3c => MotionWakup,
        0x3d => Start,
        0x3e => Select,
        0x40 => Vx,
        0x41 => Vy,
        0x42 => Vz,
        0x43 => Vbrx,
        0x44 => Vbry,
        0x45 => Vbrz,
        0x46 => Vno,
        0x47 => FeatureNotification,
        0x48 => ResolutionMultiplier,
        0x49 => Qx,
        0x50 => Qy,
        0x51 => Qz,
        0x52 => Qw,
        0x80 => SystemControl,
        0x81 => SystemPowerDown,
        0x82 => SystemSleep,
        0x83 => SystemWakeUp,
        0x84 => SystemContextMenu,
        0x85 => SystemMainMenu,
        0x86 => SystemAppMenu,
        0x87 => SystemMenuHelp,
        0x88 => SystemMenuExit,
        0x89 => SystemMenuSelect,
        0x8a => SystemMenuRight,
        0x8b => SystemMenuLeft,
        0x8c => SystemMenuUp,
        0x8d => SystemMenuDown,
        0x8e => SystemColdRestart,
        0x8f => SystemWarmRestart,
        0x90 => DpadUp,
        0x91 => DpadDown,
        0x92 => DpadRight,
        0x93 => DpadLeft,
        0x94 => IndexTrigger,
        0x95 => PalmTrigger,
        0x96 => Thumbstick,
        0x97 => SystemFunctionShift,
        0x98 => SystemFunctionShiftLock,
        0x99 => SystemFunctionShiftLockIndicator,
        0x9a => SystemDismissNotification,
        0x9b => SystemDoNotDisturb,
        0xa0 => SystemDock,
        0xa1 => SystemUndock,
        0xa2 => SystemSetup,
        0xa3 => SystemBreak,
        0xa4 => SystemDebuggerBreak,
        0xa5 => ApplicationBreak,
        0xa6 => ApplicationDebuggerBreak,
        0xa7 => SystemSpeakerMute,
        0xa8 => SystemHibernate,
        0xa9 => SystemMicrophoneMute,
        0xb0 => SystemDisplayInvert,
        0xb1 => SystemDisplayInternal,
        0xb2 => SystemDisplayExternal,
        0xb3 => SystemDisplayBoth,
        0xb4 => SystemDisplayDual,
        0xb5 => SystemDisplayToggleIntExtMode,
        0xb6 => SystemDisplaySwapPrimarySecondary,
        0xb7 => SystemDisplayToggleLCDAutoscale,
        0xc0 => SensorZone,
        0xc1 => Rpm,
        0xc2 => CoolantLevel,
        0xc3 => CoolantCriticalLevel,
        0xc4 => CoolantPump,
        0xc5 => ChassisEnclosure,
        0xc6 => WirelessRadioButton,
        0xc7 => WirelessRadioLED,
        0xc8 => WirelessRadioSliderSwitch,
        0xc9 => SystemDisplayRotationLockButton,
        0xca => SystemDisplayRotationLockSliderSwitch,
        0xcb => ControlEnable,
        0xd0 => DockableDeviceUniqueId,
        0xd1 => DockableDeviceVendorId,
        0xd2 => DockableDevicePrimaryUsagePage,
        0xd3 => DockableDevicePrimaryUsageId,
        0xd4 => DockableDeviceDockingState,
        0xd5 => DockableDeviceDisplayOcclusion,
        0xd6 => DockableDeviceObjectType,
        0xe0 => CallActiveLED,
        0xe1 => CallMuteToggle,
        0xe2 => CallMuteLED,
    }
}
