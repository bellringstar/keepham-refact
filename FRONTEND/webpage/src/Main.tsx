// React
import React from "react";
import ReactDOM from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Provider } from "react-redux";
import { store } from "@/Store/store.ts";

// Pages
import App from "@/App/App.tsx";
import Landing from "@/Pages/Landing/Landing.tsx";
import Auth from "@/Pages/Auth/Auth.tsx";
// import UserInfo from "@/Pages/UserInfo/UserInfo.tsx";
import UserInfo from "@/Pages/UserInfo/UserInfo.tsx";
// import Main from "@/Pages/Main/Landing.tsx";
// import User from "./Pages/User/User.tsx";
import RoomList from "./Pages/RoomList/RoomList.tsx";
import CreateRoom from "./Pages/CreateRoom/CreateRoom.tsx";
import ChatRoom, {
  loader as chatRoomLoader,
} from "./Pages/ChatRoom/Chatroom.tsx";
import Terms from "./Pages/Terms/Terms.tsx";
import AboutMe from "./Pages/AboutMe/AboutMe.tsx";
import ContactUs from "./Pages/ContactUs/ContactUs.tsx";
import Admin from "./Pages/Admin/Admin.tsx";
import AddBox from "./Pages/Admin/AddBox.tsx";
import Payment from "./Pages/Payment/Payment.tsx";
import Point from "./Pages/Point/Point.tsx";

// Styles
import "./Styles/global.ts";
import { createTheme, ThemeProvider } from "@mui/material";

// React Router
const router = createBrowserRouter([
  {
    path: "/",
    element: <Landing />,
  },
  {
    path: "/Home",
    element: <App />,
    children: [
      {
        path: "/Home/RoomList",
        element: <RoomList />,
      },
      {
        path: "/Home/chatRoom/:roomId",
        element: <ChatRoom />,
        loader: chatRoomLoader,
      },
      {
        path: "/Home/CreateRoom",
        element: <CreateRoom />,
      },
      {
        path: "/Home/Terms",
        element: <Terms />,
      },
      {
        path: "/Home/AboutMe",
        element: <AboutMe />,
      },
      {
        path: "/Home/ContactUs",
        element: <ContactUs />,
      },
      {
        path: "/Home/UserInfo",
        element: <UserInfo />,
      },
      {
        path: "/Home/Terms",
        element: <Terms />,
      },
      {
        path: "/Home/AboutMe",
        element: <AboutMe />,
      },
      {
        path: "/Home/ContactUs",
        element: <ContactUs />,
      },
      {
        path: "/Home/Admin",
        element: <Admin />,
      },
      {
        path: "/Home/Admin/AddBox",
        element: <AddBox />,
      },
      {
        path: "/Home/Payment",
        element: <Payment />,
      },
      {
        path: "/Home/Point",
        element: <Point />,
      },
    ],
  },
  {
    path: "Auth",
    element: <Auth />,
  },
]);

// Mui Theme
declare module "@mui/material/styles" {
  interface Palette {
    gray: Palette["primary"];
    darkgray: Palette["primary"];
  }

  interface PaletteOptions {
    gray?: PaletteOptions["primary"];
    darkgray?: PaletteOptions["primary"];
  }
}

const theme = createTheme({
  typography: {
    fontFamily: "Pretendard",
    htmlFontSize: 10,
  },
  palette: {
    gray: {
      main: "#8F95A1",
      light: "#B6BAC3",
      dark: "#6C7280",
    },
    darkgray: {
      main: "#4A4E5A",
      light: "#5B616E",
      dark: "#40434C",
    },
  },
});

declare module "@mui/material/Button" {
  interface ButtonPropsColorOverrides {
    gray: true;
    darkgray: true;
  }
}

ReactDOM.createRoot(document.getElementById("root") as HTMLElement).render(
  <React.StrictMode>
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <RouterProvider router={router} />
      </ThemeProvider>
    </Provider>
  </React.StrictMode>
);
