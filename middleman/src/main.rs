// https://docs.rs/adb_client/0.4.1/adb_client/

mod hid_joystick;
// mod nt;

use hid_joystick::HidController;

use std::io::{self, prelude::*, BufReader, Write};
use std::net::TcpStream;
use std::time::Duration;

fn main() {
    let stream = TcpStream::connect("127.0.0.1:1234").unwrap();
    stream
        .set_read_timeout(Some(Duration::from_millis(10)))
        .unwrap();
    let mut reader = BufReader::new(&stream);
    let mut buf = [0u8; 1];
    loop {
        match reader.read(&mut buf) {
            Ok(num_bytes_read) => {
                if num_bytes_read > 0 {
                    match buf[0] {
                        2 => {
                            let mut buf = [0u8; 2];
                            reader.read(&mut buf).unwrap();
                            println!("{:?}", buf);
                        }
                        3 => {
                            let mut buf = [0u8; 8];
                            reader.read(&mut buf).unwrap();
                            let val = f64::from_be_bytes(buf);
                            println!("{:?}", val);
                        }
                        _ => {}
                    }
                }
            }
            _ => {}
        }
    }
}
