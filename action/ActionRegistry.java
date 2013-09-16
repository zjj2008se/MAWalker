package action;

public class ActionRegistry {
	public static enum Action {
		NOTHING,
		LOGIN,
		GET_FLOOR_INFO, // 将会刷新area和floor
		ADD_AREA,
		GOTO_FLOOR,	// 快速取得AP，BC以及经验值和物品等信息
		GET_FAIRY_LIST,
		//PRIVATE_FAIRY_BATTLE, //国服没里妖
		EXPLORE,
		GET_FAIRY_REWARD,
		//GUILD_TOP,	//骑士团没开
		//GUILD_BATTLE,	//骑士团没开
		SELL_CARD,
		LV_UP
	}
}
