
pub type NtHandle = u32;
pub type NtConnectionDataLogger = NtHandle;
pub type NtDataLogger = NtHandle;
pub type NtEntry = NtHandle;
pub type NtInst = NtHandle;
pub type NtListener = NtHandle;
pub type NtListenerPoller = NtHandle;
pub type NtMultiSubscriber = NtHandle;
pub type NtTopic = NtHandle;
pub type NtSubscriber = NtHandle;
pub type NtPublisher = NtHandle;

pub const NT_DEFAULT_PORT3: u32 = 1735;
pub const NT_DEFAULT_PORT4: u32 = 5810;

macro_rules! c_enum {
    ($strct:ident($ty:ty) -> $($name:ident = $val:expr),* $(,)?) => {
        #[derive(Debug, Copy, Clone, PartialEq, Eq)]
        pub enum $strct {
            $($name),*
        }

        impl Into<$ty> for $strct {
            fn into(self) -> $ty {
                match self {
                    $($strct::$name => {$val}),*
                }
            }
        }
    };
}

c_enum! { NtType(u16) ->
    NtUnassigned = 0,
    NtBoolean = 0x01,
    NtDouble = 0x02,
    NtString = 0x04,
    NtRaw = 0x08,
    NtBooleanArray = 0x10,
    NtDoubleArray = 0x20,
    NtStringArray = 0x40,
    NtRpc = 0x80,
    NtInteger = 0x100,
    NtFloat = 0x200,
    NtIntegerArray = 0x400,
    NtFloatArray = 0x800
}

c_enum! { NtEntryFlags(u8) ->
    NtPersistent = 0x01,
    NtRetained = 0x02
}

c_enum! { NtLogLevel(u8) ->
    NtLogCritical = 50,
    NtLogError = 40,
    NtLogWarning = 30,
    NtLogInfo = 20,
    NtLogDebug = 10,
    NtLogDebug1 = 9,
    NtLogDebug2 = 8,
    NtLogDebug3 = 7,
    NtLogDebug4 = 6
}

c_enum! { NtNetworkMode(u8) ->
    NtNetModeNone = 0x00,     /* not running */
    NtNetModeServer = 0x01,   /* running in server mode */
    NtNetModeClient3 = 0x02,  /* running in NT3 client mode */
    NtNetModeClient4 = 0x04,  /* running in NT4 client mode */
    NtNetModeStarting = 0x08, /* flag for starting (either client or server) */
    NtNetModeLocal = 0x10,    /* running in local-only mode */
}

c_enum! { NtEventFlags(u8) ->
    NtEventNone = 0,
    /* Initial listener addition. */
    NtEventImmediate = 0x01,
    /* Client connected (on server, any client connected). */
    NtEventConnected = 0x02,
    /* Client disconnected (on server, any client disconnected). */
    NtEventDisconnected = 0x04,
    /* Any connection event (connect or disconnect). */
    NtEventConnection = (0x02 | 0x04),
    /* New topic published. */
    NtEventPublish = 0x08,
    /* Topic unpublished. */
    NtEventUnpublish = 0x10,
    /* Topic properties changed. */
    NtEventProperties = 0x20,
    /* Any topic event (publish, unpublish, or properties changed). */
    NtEventTopic = (0x08 | 0x10 | 0x20),
    /* Topic value updated (via network). */
    NtEventValueRemote = 0x40,
    /* Topic value updated (local). */
    NtEventValueLocal = 0x80,
    /* Topic value updated (network or local). */
    NtEventValueAll = (0x40 | 0x80),
    /* Log message. */
    NtEventLogmessage = 0x100,
    /* Time synchronized with server. */
    NtEventTimesync = 0x200
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum NtValueImpl {
    Boolean(bool),
    Int(i64),
    Float(f32),
    Double(f64),
    String(String),
    Raw(Vec<u8>),
    BoolArray(Vec<bool>),
    DoubleArray(Vec<f64>),
    FloatArray(Vec<f32>),
    IntArray(Vec<i64>),
    StringArray(Vec<String>)
}

#[derive(Debug, Clone)]
pub struct NtValue {
    type_: NtType,
    last_change: i64,
    server_time: i64,
    data: NtValueImpl
}

#[derive(Debug, Clone)]
pub struct NtTopicInfo {
    topic: NtTopic,
    name: String,
    type_: NtType,
    type_str: String,
    properties: String
}

#[derive(Debug, Clone)]
pub struct NtConnectionInfo {
    remote_id: String,
    remote_ip: String,
    remote_port: u32,
    last_update: u64,
    protocol_version: u32
}

#[derive(Debug, Clone)]
pub struct NtValueEventData {
    topic: NtTopic,
    subentry: NtHandle,
    value: NtValue
}

#[derive(Debug, Clone)]
pub struct NtLogMessage {
    level: u32,
    filename: String,
    line: u32,
    message: String
}

#[derive(Debug, Clone)]
pub struct NtTimeSyncEventData {
    server_time_offset: i64,
    rtt2: i64,
    valid: bool
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum NtEventImpl {
    Connection(NtConnectionInfo),
    Topic(NtTopicInfo),
    Value(NtValueEventData),
    LogMessage(NtLogMessage),
    TimeSync(NtTimeSyncEventData)
}

#[derive(Debug, Clone)]
pub struct NtEvent {
    listener: NtHandle,
    flags: u32,
    data: NtEventImpl
}


