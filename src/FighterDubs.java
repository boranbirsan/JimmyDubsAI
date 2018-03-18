import aiinterface.CommandCenter;
import aiinterface.AIInterface;

import struct.FrameData;
import struct.GameData;
import struct.Key;


public class FighterDubs implements AIInterface {

	private Key inputKey;
	private boolean playerNumber;
	private CommandCenter cc;
	
	private FrameData frameData;
	
	@Override
	public void close() {
	}

	@Override
	public void getInformation(FrameData frameData) {
		this.frameData = frameData;
		cc.setFrameData(this.frameData, playerNumber);
	}

	@Override
	public int initialize(GameData gameData, boolean playerNumber) {
		this.playerNumber = playerNumber;
		this.inputKey = new Key();
		cc = new CommandCenter();
		frameData = new FrameData();
		return 0;
	}

	@Override
	public Key input() {
		// TODO Auto-generated method stub
		return inputKey;
	}

	@Override
	public void processing() {
		if(!frameData.getEmptyFlag() && frameData.getRemainingFramesNumber()> 0){
			if(cc.getSkillFlag()){
				inputKey = cc.getSkillKey();
			}else {
				inputKey.empty();
				cc.skillCancel();
				if(frameData.getDistanceX() < 100){
					cc.commandCall("CROUCH_FB");
				}else if(frameData.getDistanceX() > 300){
					cc.commandCall("2 3 6 _ A");
				}
			}
		}
	}

	@Override
	public void roundEnd(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

}
