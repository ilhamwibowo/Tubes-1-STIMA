package za.co.entelect.challenge.command;

public class TweetCommand implements Command {

    private int lane;
    private int block;

    public TweetCommand(int lane, int block) {
        this.lane = lane;
        this.block = block;
    }

    public int getLane() {
        return this.lane;
    }

    public int getBlock() {
        return this.block;
    }

    @Override
    public String render() {
        return String.format("USE_TWEET %d %d", this.getLane(), this.getBlock());
    }
}