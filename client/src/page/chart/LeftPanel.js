import React from 'react';
import { Grid, IconButton, Typography, Box, TextField, Button } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import MenuIcon from '@mui/icons-material/Menu';
import CloseIcon from '@mui/icons-material/Close';

const LeftPanel = ({
  members,
  onDeleteMember,
  onAddMember,
  onChangeMember,
  partitionCount,
  setPartitionCount,
  replicationFactor,
  setReplicationFactor,
  onSubmit,
  isPanelOpen,
  togglePanel
}) => {
  return (
    <Box p={2} sx={{ backgroundColor: '#f5f5f5', height: '100vh', overflow:"auto" }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6" gutterBottom>
          Consistent Hashing Parameters
        </Typography>
        <IconButton onClick={togglePanel}>
          {isPanelOpen ? <CloseIcon /> : <MenuIcon />}
        </IconButton>
      </Box>
      <TextField
        label="Partition Count"
        type="number"
        value={partitionCount}
        onChange={(e) => setPartitionCount(e.target.value)}
        fullWidth
        margin="normal"
      />
      <TextField
        label="Replication Factor"
        type="number"
        value={replicationFactor}
        onChange={(e) => setReplicationFactor(e.target.value)}
        fullWidth
        margin="normal"
      />
      <Typography variant="h6" gutterBottom>
        Members
      </Typography>
      <Grid container spacing={2}>
        {members.map((member, index) => (
          <Grid item xs={12} key={index}>
            <Box
              display="flex"
              justifyContent="space-between"
              alignItems="center"
              p={1}
              sx={{ backgroundColor: '#ffffff', borderRadius: '4px' }}
            >
              <TextField
                label={`Node ${index + 1}`}
                value={member.name}
                onChange={(e) => onChangeMember(index, e.target.value)}
                fullWidth
              />
              <IconButton
                edge="end"
                color="secondary"
                onClick={() => onDeleteMember(index)}
              >
                <DeleteIcon />
              </IconButton>
            </Box>
          </Grid>
        ))}
        <Grid item xs={12}>
          <Button variant="contained" color="primary" onClick={onAddMember} size="small">
            Add Node
          </Button>
        </Grid>
        <Grid item xs={12}>
          <Button variant="contained" color="secondary" onClick={onSubmit} fullWidth>
            Submit
          </Button>
        </Grid>
      </Grid>
    </Box>
  );
};

export default LeftPanel;
