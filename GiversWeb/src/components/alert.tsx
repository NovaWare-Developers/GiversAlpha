import React from "react";
import Snackbar from "@material-ui/core/Snackbar";
import MuiAlert, { AlertProps } from "@material-ui/lab/Alert";

function Alert(props: AlertProps) {
  return <MuiAlert elevation={5} variant="filled" {...props} />;
}

export default function CustomizedSnackbars(props) {

  const handleClose = (event?: React.SyntheticEvent, reason?: string) => {
    if (reason === "clickaway") {
      return;
    }

    props.setOpen(false);
  };

  return (
    <div>
      <Snackbar open={props.open} autoHideDuration={5000} onClose={handleClose}>
        <Alert onClose={handleClose} severity={props.severity}>
          {props.description}
        </Alert>
      </Snackbar>
    </div>
  );
}

/*
Severity:
-error
-warning
-info
-success
*/