#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <arpa/inet.h>

int main(int argc, char *argv[]) {

    FILE *fp;

    if (argc != 3) {
        puts("Usage: summary <infile> <outfile>");
        return 1;
    }

    if ((fp=fopen(argv[1],"rb")) == 0) {
        perror("Cannot find in file");
        return 2;
    }

    FILE *fout = fopen(argv[2], "w");
    if (fout == NULL) {
        perror("Error opening out file!");
        return 3;
    }

    for (;;) {
        unsigned char ip_header_length;
        unsigned short ip_packet_length;
        unsigned char tcp_header_length;

        fread(&ip_header_length, 1, 1, fp);
        ip_header_length = ip_header_length & 0x0F;
        fseek(fp, 1, SEEK_CUR);
        fread(&ip_packet_length, sizeof(short), 1, fp);
        ip_packet_length = ntohs(ip_packet_length);
        fseek(fp, 4 * (ip_header_length - 1), SEEK_CUR); //TCP header
        fseek(fp, 12, SEEK_CUR);
        fread(&tcp_header_length, 1, 1, fp);
        tcp_header_length = tcp_header_length >> 4;
        fseek(fp, 4 * (tcp_header_length - 4) + 3, SEEK_CUR); //Start of data

        int data_length = ip_packet_length - 4 * (ip_header_length + tcp_header_length);
        char bytes[data_length];
        int r = fread(bytes, 1, data_length, fp);
        if (r < data_length) {
            break;
        }
        fwrite(bytes, 1, data_length, fout);
    }

    fclose(fout);

    return 0;
}
