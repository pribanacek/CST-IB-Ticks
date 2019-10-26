#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>

int main(int argc, char *argv[]) {

    FILE *fp;

    if (argc != 2) {
        puts("Usage: summary <logfile>");
        return 1;
    }

    if ((fp=fopen(argv[1],"rb")) == 0) {
        perror("Cannot find log file");
        return 2;
    }

    unsigned char source[4];
    char* source_address = malloc(15 * sizeof(char));

    unsigned char dest[4];
    char* dest_address = malloc(15 * sizeof(char));

    unsigned char ip_header_length; //number of 32-bit words
    unsigned short ip_packet_length;
    unsigned char tcp_header;
    int total_packets = 1;

    //fp is at 0
    //this reads the two 4-bit fields - version and header length
    fread(&ip_header_length, sizeof(char), 1, fp);
    //bitmask out the version
    ip_header_length = ip_header_length & 0x0F;

    fseek(fp, 2, SEEK_SET);
    fread(&ip_packet_length, sizeof(short), 1, fp);
    //fix byte order
    ip_packet_length = ntohs(ip_packet_length);

    fseek(fp, 12, SEEK_SET);
    fread(&source, sizeof(char), 4, fp);
    sprintf(source_address, "%d.%d.%d.%d", source[0], source[1], source[2], source[3]);
    //fp is now at position 16
    fread(&dest,  sizeof(char), 4, fp);
    sprintf(dest_address, "%d.%d.%d.%d", dest[0], dest[1], dest[2], dest[3]);

    //jump to end of IP header to start on the TCP header
    fseek(fp, 4 * ip_header_length, SEEK_SET);

    fseek(fp, 12, SEEK_CUR);
    //reads 4-bit data offset and beginning of Reserved
    fread(&tcp_header, sizeof(char), 1, fp);
    //shift extra stuff out, so we only have the header length
    tcp_header = tcp_header >> 4;

    //go to end of first packet
    fseek(fp, ip_packet_length, SEEK_SET);

    for (;;) {
        unsigned short current_packet_length;
        fseek(fp, 2, SEEK_CUR);
        int r = fread(&current_packet_length, sizeof(short), 1, fp);
        current_packet_length = ntohs(current_packet_length);
        if (r <= 0) {
            break;
        }
        fseek(fp, current_packet_length - 2, SEEK_CUR);
        total_packets++;
    }

    printf("%s %s %d %d %d %d \n", source_address, dest_address,
            ip_header_length, ip_packet_length, tcp_header, total_packets);

    free(source_address);
    free(dest_address);

    return 0;
}
