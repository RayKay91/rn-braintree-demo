import React from 'react';
import {View, Text, Button, StyleSheet, NativeModules} from 'react-native';
const {BTDropIn} = NativeModules;

const clientToken = 'clientTokenGoesHere'

 const options = {
  clientToken,
  forename: 'bob',
  surname: 'builder',
  email: 'bob@builder.com',
  addressLine1: '10 Downing Street',
  city: 'London',
  postcode: 'SW1A 2AA',
  amount: '15',
};

const App = () => {
  const handlePress = () => {
    BTDropIn.show(options)
      .then(r => console.log(r))
      .catch(e => console.log(e));
  };
  return (
    <View style={styles.container}>
      <Text>Hello World!</Text>
      <Button title="Press me!" onPress={handlePress} />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default App;
