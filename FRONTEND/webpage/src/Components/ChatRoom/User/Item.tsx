import { useEffect, useState } from "react";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemText from "@mui/material/ListItemText";
import Typography from "@mui/material/Typography";
import Button from "@mui/material/Button";
import Divider from "@mui/material/Divider";
import axios from "axios";
import ListItemIcon from "@mui/material/ListItemIcon";
import StarIcon from "@mui/icons-material/Star";
import { Box } from "@mui/material";

type ItemProps = {
  id: number;
  name: string;
  roomId: number;
  userNick: string | null;
  superNick: string | null;
  updateInfo: () => void;
};

function Item(props: ItemProps) {
  // let itemStyleByAmountType = "item " + props.amountType;
  // let fontStyleByAmountType = "fs-emphasis fc-red";

  const roomId = props.roomId;
  const itemTitle = props.name;
  const userNick = props.userNick;
  const superNick = props.superNick;
  const AccessToken = sessionStorage.getItem("AccessToken");

  // let itemAmount = "-" + addComma(props.amount.toString());

  // if (props.amountType === "충전") {
  //   fontStyleByAmountType = fontStyleByAmountType.replace("red", "green");
  //   itemAmount = itemAmount.replace("-", "+");
  // }

  const [exitUserSwitch, setExitUserSwitch] = useState(false);
  const [transSuperSwitch, setTransSuperSwitch] = useState(false);

  useEffect(() => {
    const exitUser = async () => {
      console.log("Access Token", AccessToken);
      try {
        const url = import.meta.env.VITE_URL_ADDRESS + "/api/rooms/kick";
        const response = await axios.put(
          url,
          {
            kicked_user_nick_name: itemTitle,
            room_id: roomId,
          },
          {
            headers: {
              Authorization: `Bearer ` + AccessToken,
            },
          }
        );
        console.log(response.data.body);

        console.log(response);
        props.updateInfo();
      } catch (error) {
        console.log(error);
      }
    };

    if (exitUserSwitch) {
      exitUser();
      setExitUserSwitch(true);
    }
  }, [exitUserSwitch]);

  useEffect(() => {
    const transSuper = async () => {
      try {
        const url = import.meta.env.VITE_URL_ADDRESS + "/api/rooms/superUser";
        const response = await axios.put(
          url,
          {
            room_id: roomId,
            new_super_user: itemTitle,
          },
          {
            headers: {
              Authorization: `Bearer ` + AccessToken,
            },
          }
        );
        console.log(response.data.body);

        console.log(response);
        props.updateInfo();
      } catch (error) {
        console.log(error);
      }
    };
    if (transSuperSwitch) {
      transSuper();
      setTransSuperSwitch(false);
    }
  }, [transSuperSwitch]);

  return (
    <div>
      <List>
        {userNick === superNick && itemTitle !== superNick ? (
          <ListItem
            secondaryAction={
              <Box sx={{
                display: "flex",
                gap: 2,
              }}>
                <Button
                  variant="contained"
                  onClick={() => {
                    setExitUserSwitch(true);
                  }}
                >
                  <Typography>추방</Typography>
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => {
                    setTransSuperSwitch(true);
                  }}
                >
                  <Typography>위임</Typography>
                </Button>
              </Box>
            }
          >
            <ListItemText primary={itemTitle} />
          </ListItem>
        ) : (
          <ListItem>
            <ListItemText primary={itemTitle} />
            {itemTitle === superNick ? (
              <ListItemIcon>
                <StarIcon />
              </ListItemIcon>
            ) : null}
          </ListItem>
        )}
        <Divider />
      </List>
    </div>
  );
}

export default Item;
