# Example script to dump the logged apdu commands
# from the card and format them.

from smartcard.System import readers

g_readers = readers()

def reader_select(rid=0):
    if not g_readers:
        print("No smart card readers found.")
        return None 
    
    return g_readers[rid]

def reader_connect(selected_reader):
    if selected_reader is None:
        selected_reader = 0
        return None
    
    connection = selected_reader.createConnection()
    connection.connect()

    print(f"Connected to {selected_reader}")
    return connection

def reader_disconnect(connection, selected_reader):
    if connection is None:
        print("No connection to disconnect.")
        return
    
    connection.disconnect()
    print(f"Disconnected from {selected_reader}")

def send_apdu(command, connection):
    try:
        response = connection.transmit(command)
        return response
    except Exception as e:
        print(f"An error occurred: {e}")
        return None

def buffer_get_dump_size(connection):
    get_size_command = [0xEE, 0xF2, 0x00, 0x00]
    response, _, _ = send_apdu(get_size_command, connection)

    size_val = int.from_bytes(response, byteorder='big')  

    return size_val

def buffer_get_offsets(total_size):
    segment_size = 256
    offsets = []
    num_segments = total_size // segment_size
    
    for i in range(num_segments):
        high_byte = i // 256  # Calculate the high byte
        low_byte = i % 256    # Calculate the low byte
        offsets.append([high_byte, low_byte])  # Store as a list
    
    # If there's a remainder, handle the last segment offset
    if total_size % segment_size > 0:
        high_byte = num_segments // 256
        low_byte = num_segments % 256
        offsets.append([high_byte, low_byte])  # Store as a list
    
    return offsets

def buffer_read_all(connection, offsets):
    full_byte_array = bytearray()
    offset_count = len(offsets)

    for i in range(offset_count):
        high_byte, low_byte = offsets[i]
        read_buffer_command = [0xEE, 0xF0, high_byte, low_byte]
        response, _, _ = send_apdu(read_buffer_command, connection)

        if response:
            full_byte_array.extend(response)

    return full_byte_array

def tokenize_buffer(buffer_content, delimiter=(0xDE, 0xAD)):
    delimiter_bytes = bytearray(delimiter)
    tokenized_lines = []
    current_line = bytearray()

    for byte in buffer_content:
        current_line.append(byte)
    
        if len(current_line) >= len(delimiter_bytes) and current_line[-len(delimiter_bytes):] == delimiter_bytes:
            tokenized_lines.append(bytes(current_line[:-len(delimiter_bytes)]))
            current_line.clear()

    if current_line:
        tokenized_lines.append(bytes(current_line))

    return tokenized_lines

def print_tokenized_lines(tokenized_output):
    for line in tokenized_output:
        if len(line) > 300 and all(byte == 0 for byte in line[:300]):
            continue
        hex_representation = ' '.join(f'{byte:02X}' for byte in line)
        ascii_representation = ''.join(chr(byte) if 32 <= byte <= 126 else '.' for byte in line)
        print(f"Hex:   {hex_representation}")
        print(f"ASCII: {ascii_representation}")
        print('-' * 50)

def main():
    sel_reader = reader_select()
    reader_handle = reader_connect(sel_reader)

    dump_buf_size = buffer_get_dump_size(reader_handle)
    required_dump_offsets = buffer_get_offsets(dump_buf_size)
    buffer_content = buffer_read_all(reader_handle, required_dump_offsets)
    tokenized_output = tokenize_buffer(buffer_content)

    print_tokenized_lines(tokenized_output)

    reader_disconnect(reader_handle, sel_reader)

main()