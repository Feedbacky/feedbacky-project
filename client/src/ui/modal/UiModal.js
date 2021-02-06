import styled from "@emotion/styled";
import PropTypes from "prop-types";
import React from "react";
import Modal from "react-bootstrap/Modal";

const StyledModal = styled(Modal)`
  .modal-content {
    background-color: hsl(210, 17%, 98%);
    box-shadow: var(--box-shadow) !important;
    
    .dark & {
      background-color: var(--dark-background);
      color: var(--dark-font-color) !important;
      box-shadow: var(--dark-box-shadow) !important;
    }
  }
`;

const Header = styled(Modal.Header)`
  display: block;
  border-bottom: medium none;
  text-align: center;
  padding-bottom: 0;
`;

const Body = styled(Modal.Body)`
  padding: .25rem 1.5rem;
`;

const Footer = styled(Modal.Footer)`
  border-top: medium none;
  padding: .5rem 1.5rem 1rem 1.5rem;
`;

const UiModal = (props) => {
    const {id, isOpen, onHide, header, footer = null, children, size, ...otherProps} = props;
    return <StyledModal size={size} id={id} show={isOpen} onHide={onHide} centered {...otherProps}>
        <Header>{header}</Header>
        <Body>{children}</Body>
        <Footer>{footer}</Footer>
    </StyledModal>
};


UiModal.propTypes = {
    id: PropTypes.string.isRequired,
    isOpen: PropTypes.bool.isRequired,
    onHide: PropTypes.func.isRequired,
    header: PropTypes.object,
    footer: PropTypes.object
};
export {UiModal};