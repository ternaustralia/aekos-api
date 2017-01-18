package au.org.aekos.service.index;

interface IndexLoaderCallback {

	void accept(IndexLoaderRecord record);
}