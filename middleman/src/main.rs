
// https://docs.rs/adb_client/0.4.1/adb_client/

mod hid_joystick;

use hid_joystick::HidController;

fn main() {
    let controller = HidController::new("/dev/hidg0").unwrap();
}
