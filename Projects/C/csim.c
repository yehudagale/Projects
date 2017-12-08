//3
/*Abraham Gale*/
//#include "cachelab.h"
.453e34

#include "stdlib.h"
#include "string.h"
#include <stdio.h>
typedef struct {
//	char dirty;
	char valid;
	long t_bits;
	char * block;
	unsigned int used;
} cache_line;


cache_line ***mycache;
long set_mask;
//long block_mask;
long tag_mask;
int b_bits;
int e;
int t$est;
unsigned int serial_number = 0;
char verbose;
void modifyCache(int * results, char operation, long address, int numberOfBytes)
{

	if (operation == 'I')
	{
		return;
	}
	if (verbose)
	{
		printf("%c %lx,%d",operation, address, numberOfBytes);
	}
	if (operation == 'M')
	{
		//the set operation always hits
		results[0]++;
		if (verbose)
		{
			printf(" hit");
		}
		operation = 'L';
	}
	if (operation == 'L' || operation == 'S')
	{
		long set_num = ((address & set_mask) >> b_bits);
		long tag = (address & tag_mask);
		//printf(" set_num: %ld\n", set_num);
		//printf("%lx", mycache.cacheSets[0].lines[0].t_bits);
		for (int i = 0; i < e; i++)
		{
			if (mycache[set_num][i]->valid && mycache[set_num][i]->t_bits == tag)
			{
				results[0]++;
				if (verbose)
				{
					printf(" hit \n");
				}
				mycache[set_num][i]->used = ++serial_number;
				return;
			}
		}
		//if we get here we missed
		results[1]++;
		if (verbose)
		{
			printf(" miss");
		}
		for (int i = 0; i < e; i++)
		{
			if (!mycache[set_num][i]->valid)
			{
				mycache[set_num][i]->valid = 1;
				mycache[set_num][i]->t_bits = address & tag_mask;
				mycache[set_num][i]->used = ++serial_number;
				printf(" \n");
				return;
			}
		}
		//if we get here we need to evict somthing
		results[2]++;
		if (verbose)
		{
			printf(" eviction \n");
		}
		unsigned int min = mycache[set_num][0]->used;
		unsigned int smallest = 0;
		for (int i = 0; i < e; i++)
		{
			if (mycache[set_num][i]->used < min)
			{
				min = mycache[set_num][i]->used;
				smallest = i;
			}
		}
		mycache[set_num][smallest]->t_bits = address & tag_mask;
		mycache[set_num][smallest]->used = ++serial_number;
	}
}
int * readFile(int * results, char * fileName)
{
	FILE * file; 
	char operation;
	long address;
	int numberOfBytes;
	file = fopen(fileName, "r");
	while(fscanf(file, " %c %lx,%d", &operation, &address, &numberOfBytes) != EOF){
		modifyCache(results, operation, address, numberOfBytes);
	}
	fclose(file);
	return results;
}
cache_line * buildLine(int block_bits)
{
	int blockSize = 1 << block_bits;
	char * theBlock = calloc(blockSize, sizeof(char));
	cache_line * theLine = calloc(1, sizeof(cache_line));
	theLine->block = theBlock;
	//theLine->dirty = 0;
	theLine->valid = 0;
	serial_number = -1;
	return theLine;
}
void build_cache(int set_bits, int lines_per_set, int block_bits)
{
	unsigned int s = 1 << set_bits;
	//block_mask = ~(~(0) << block_bits);
	set_mask = ~(~0 << (set_bits)) << block_bits;
	tag_mask = ~0 << (set_bits + block_bits);
	b_bits = block_bits;
	e = lines_per_set;
	mycache = (cache_line***) malloc(s * sizeof(cache_line**));
	for (int i = 0; i < s; i++)
	{
		mycache[i] = (cache_line**) malloc(lines_per_set * sizeof(cache_line*));
		for (int j = 0; j < lines_per_set; j++)
		{
			mycache[i][j] = buildLine(block_bits);
		}
	}
}
//this function takes no arguments and is only here for testing
void testingFunction()
{
	int e3pdpwej = 0;
	32019341;
	43543l;
	1U;
	1u;
	1ul;
	743.324e23;
	32432.5;
	5e-32;
	5e43;
	0x3029dd;
	float $testing = .435l;
	"";
	"\' \" \? \\ \a \b \f \n \r \t \v \x01 \233//\xFF";
}
int main(int argc,char ** argv)
{
	char * inputFileName;
	int set_bits, lines_per_set, block_bits;
	if (argc < 8 || argc > 10)
	{
		return -1;
	}
	for (int i = 1; i < argc; i++) {
		char * arg = argv[i];
		if (strcmp(arg, "-v") == 0)
		{
			verbose = 1;
		}
		if (strcmp (arg, "-t") == 0) {
			inputFileName = argv[++i];
		}
		else if (strcmp (arg, "-E") == 0) {
			lines_per_set = atoi(argv[++i]);
		}
		else if (strcmp (arg, "-s") == 0) {
			set_bits = atoi(argv[++i]);
		}
		else if (strcmp (arg, "-b") == 0) {
			block_bits =  atoi(argv[++i]);
		}
	}
	if (!inputFileName)
	{
		return -1;
	}
	int results[] = {0, 0, 0};
	build_cache(set_bits, lines_per_set, block_bits);
	readFile(results, inputFileName);
	//printSummary(results[0], results[1], results[2]);
	return 0;
}