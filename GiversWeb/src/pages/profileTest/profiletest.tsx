import React from 'react';
// @material-ui/core components
import withStyles from '@material-ui/core/styles/withStyles';
import InputLabel from '@material-ui/core/InputLabel';
// core components
import GridItem from '../../components/Grid/GridItem';
import GridContainer from '../../components/Grid/GridContainer';
import CustomInput from '../../components/CustomInput/CustomInput';
import Button from '../../components/CustomButtons/Button';
import Card from '../../components/Card/Card';
import CardHeader from '../../components/Card/CardHeader';
import CardAvatar from '../../components/Card/CardAvatar';
import CardBody from '../../components/Card/CardBody';
import CardFooter from '../../components/Card/CardFooter';

import avatar from '../../assets/img/faces/default.png';
import { createStyles } from '@material-ui/core';


const styles = createStyles({
  cardCategoryWhite: {
    color: 'rgba(255,255,255,.62)',
    margin: '0',
    fontSize: '14px',
    marginTop: '0',
    marginBottom: '0'
  },
  cardTitleWhite: {
    color: '#FFFFFF',
    marginTop: '0px',
    minHeight: 'auto',
    fontWeight: 300,
    fontFamily: '\'Roboto\', \'Helvetica\', \'Arial\', sans-serif',
    marginBottom: '3px',
    textDecoration: 'none'
  }
});

function UserProfile(props: any) {
  const { classes } = props;
  return (
    <div style={{marginTop:`2%`}}>
      <GridContainer>
        <GridItem xs={12} sm={12} md={8}>
          <Card>
            <CardHeader color="primary">
              <h4 className={classes.cardTitleWhite}>Editar Perfil</h4>
              <p className={classes.cardCategoryWhite}>(Em Desenvolvimento)</p>
            </CardHeader>
            <CardBody>
              <GridContainer>
                <GridItem xs={12} sm={12} md={4}>
                  <CustomInput
                    labelText="Nome"
                    id="nome"
                    formControlProps={{
                      fullWidth: true
                    }}
                    inputProps={{
                      disabled: true
                    }}
                    value={"José Coimbra"}
                  />
                </GridItem>
                <GridItem xs={12} sm={12} md={6}>
                  <CustomInput
                    labelText="Email"
                    id="email-address"
                    formControlProps={{
                      fullWidth: true
                    }}
                    value={"José Coimbra"}
                    error={true}                  />
                </GridItem>
              
              <GridContainer>
              </GridContainer>
                <GridItem xs={12} sm={12} md={4}>
                  <CustomInput
                    labelText="Telemovel"
                    id="phone"
                    formControlProps={{
                      fullWidth: true
                    }}
                    />
                </GridItem>
                <GridItem xs={12} sm={12} md={4}>
                  <CustomInput
                    labelText="Data de Nascimento"
                    id="date"
                    formControlProps={{
                      fullWidth: true
                    }}
                  />
                </GridItem>
                <GridItem xs={12} sm={12} md={4}>
                  <CustomInput
                    labelText="Género"
                    id="gender"
                    formControlProps={{
                      fullWidth: true
                    }}
                  />
                </GridItem>
              </GridContainer>
                    <GridContainer>
                      <GridItem xs={12} sm={12} md={8}>
                        <CustomInput
                          labelText="Morada"
                          id="address"
                          formControlProps={{
                            fullWidth: true
                          }}
                        />
                      </GridItem>
                      <GridItem xs={12} sm={12} md={4}>
                        <CustomInput
                          labelText="País"
                          id="country"
                          formControlProps={{
                            fullWidth: true
                          }}
                        />
                      </GridItem>
                    </GridContainer>
              <GridContainer>
                <GridItem xs={12} sm={12} md={12}>
                  
                  <CustomInput
                    labelText="Biografia"
                    id="about-me"
                    formControlProps={{
                      fullWidth: true
                    }}
                    inputProps={{
                      multiline: true,
                      rows: 5
                    }}
                  />
                </GridItem>
              </GridContainer>
            </CardBody>
            <CardFooter>
              <Button color="primary">Editar</Button>
            </CardFooter>
          </Card>
        </GridItem>
        <GridItem xs={12} sm={12} md={4}>
          <Card profile={true}>
            <CardAvatar profile={true}>
              <a href="#pablo" onClick={e => e.preventDefault()}>
                <img src={avatar} alt="..." />
              </a>
            </CardAvatar>
            <CardBody profile={true}>
              <h6 className={classes.cardCategory}>GIVER</h6>
              <h4 className={classes.cardTitle}>Username</h4>
              <p className={classes.description}>
              Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum
              </p>
              <Button color="primary" round={true} disabled={true}>
                Seguir
              </Button>
            </CardBody>
          </Card>
        </GridItem>
      </GridContainer>
    </div>
  );
}

export default withStyles(styles)(UserProfile);
