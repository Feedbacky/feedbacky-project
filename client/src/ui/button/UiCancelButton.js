import React from "react";
import {Button} from "react-bootstrap";
import styled from "@emotion/styled";

const CancelButton = styled(Button)`
  transition: var(--hover-transition) !important;
  color: hsla(0, 0%, 0%, .6);

  &:focus {
    outline: 1px dotted white;
  }

  .dark & {
    color: hsla(0, 0%, 95%, .6) !important;
  }

  &:hover {
    color: hsla(0, 0%, 0%, .6);
    transform: var(--hover-transform-scale);
  }
`;

const UiCancelButton = (props) => {
    const {children, ...otherProps} = props;
    return <CancelButton aria-label={"Cancel"} variant={"link"} {...otherProps}>{children}</CancelButton>
};

export {UiCancelButton};